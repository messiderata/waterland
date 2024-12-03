package UserHomePageDirectory.HomeFragmentUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import UserHomePageDirectory.FragmentsDirectory.HistoryFragment;
import UserHomePageDirectory.MainDashboardUser;
import UserHomePageDirectory.OrderTrackingFragments.UserPendingOrdersFragment;

public class OrderReceipt extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseDatabase rdb;
    private AddedItems addedItems;
    private Map<String, Object> currentDefaultAddress;
    private Map<String, Object> GCashPaymentDetails;
    private String modeOfPayment, additionalMessage, selectedDateValue;
    private CardView button_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_receipt);
        initializeObject();
        getIntentData();

        // Save order to Firebase
        generateUniqueDocumentId();

        // Set up button and its click listener
        button_btn.setOnClickListener(view -> {
            Intent backToHome = new Intent(OrderReceipt.this, MainDashboardUser.class);
            startActivity(backToHome);
            finish();
        });


    }

    private void initializeObject(){
        button_btn = findViewById(R.id.button_ok);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        rdb = FirebaseDatabase.getInstance();
    }

    private void getIntentData(){
        // Retrieve intent data
        Intent intent = getIntent();
        addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        currentDefaultAddress = (Map<String, Object>) intent.getSerializableExtra("deliveryAddress");
        modeOfPayment = (String) intent.getSerializableExtra("modeOfPayment");
        additionalMessage = (String) intent.getSerializableExtra("additionalMessage");
        selectedDateValue = (String) intent.getSerializableExtra("selectedDateValue");

        if (modeOfPayment.equals("GCash")){
            GCashPaymentDetails = (Map<String, Object>) intent.getSerializableExtra("GCashPaymentDetails");
        }
    }

    // flow
    // this first generate a random id from 'pendingOrders' collection
    // then it will check if that id is already existed in
    // 'waitingForCourier', 'onDelivery', 'deliveredOrders' collections
    // this way we can pass the the id from a collection to another without conflict
    // this way as well we can track the order properly without being confused because of changing the id
    // then if the ids are existed then generate again
    // else save the order details to the firebase firestore
    private void generateUniqueDocumentId() {
        String documentId = db.collection("pendingOrders").document().getId();
        checkDocumentInWaitingOrdersCollection(documentId);
    }

    private void checkDocumentInWaitingOrdersCollection(String documentId) {
        db.collection("waitingForCourier")
            .document(documentId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().exists()) {
                        // Document ID already exists in 'waitingForCourier', generate a new one
                        generateUniqueDocumentId();
                    } else {
                        // Proceed to check the next collection
                        checkDocumentInOnDelivery(documentId);
                    }
                } else {
                    Toast.makeText(OrderReceipt.this, "Failed to check waitingForCourier collection.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void checkDocumentInOnDelivery(String documentId) {
        db.collection("onDelivery")
            .document(documentId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().exists()) {
                        // Document ID already exists in 'onDelivery', generate a new one
                        generateUniqueDocumentId();
                    } else {
                        // Proceed to check the next collection
                        checkDocumentInDeliveredOrders(documentId);
                    }
                } else {
                    Toast.makeText(OrderReceipt.this, "Failed to check onDelivery collection.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void checkDocumentInDeliveredOrders(String documentId) {
        db.collection("deliveredOrders")
            .document(documentId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().exists()) {
                        // Document ID already exists in 'deliveredOrders', generate a new one
                        generateUniqueDocumentId();
                    } else {
                        // Document ID is unique across all collections
                        saveOrderWithUniqueDocumentId(documentId);
                    }
                } else {
                    Toast.makeText(OrderReceipt.this, "Failed to check deliveredOrders collection.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void saveOrderWithUniqueDocumentId(String documentId) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("date_ordered", Timestamp.now());
        orderData.put("order_icon", addedItems.getOrderIcon());
        orderData.put("order_id", documentId);
        orderData.put("order_items", addedItems.getCartItems());
        orderData.put("order_status", "ORDERED");
        orderData.put("total_amount", addedItems.getTotalAmount());
        orderData.put("delivery_address", currentDefaultAddress);
        orderData.put("user_id", mAuth.getCurrentUser().getUid());
        orderData.put("mode_of_payment", modeOfPayment);
        orderData.put("additional_message", additionalMessage);

        if (modeOfPayment.equals("GCash")){
            orderData.put("gcash_payment_details", GCashPaymentDetails);
        }

        // Retrieve the current user's 'fullName' and save it to search term
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String fullName = documentSnapshot.getString("fullName");
                String[] nameParts = fullName.split(" ");
                if (nameParts.length >= 2) {
                    orderData.put("search_term", nameParts[1]);
                } else if (nameParts.length == 1) {
                    orderData.put("search_term", nameParts[0]);
                } else {
                    orderData.put("search_term", "User no surname");
                }

                // Delivery date
                selectedDateValue = selectedDateValue.replace("Selected Date: ", "");
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                LocalDate date = LocalDate.parse(selectedDateValue, inputFormatter);
                String formattedDate = date.format(outputFormatter);
                orderData.put("date_delivery", formattedDate);

                // check if the current user has pending orders
                // if user has pending order then we append the new item
                // to the existing order of the user
                // under Map<String, Object> 'order_items' field
                // as of the request of the panelist
                db.collection("pendingOrders")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                // User has pending orders
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    // Assuming `order_items` is a List<Map<String, Object>>
                                    List<Map<String, Object>> orderItems = (List<Map<String, Object>>) document.get("order_items");

                                    if (orderItems == null) {
                                        orderItems = new ArrayList<>();
                                    }

                                    for (Map<String, Object> item : addedItems.getCartItems()) {
                                        Log.d("updated order items", "item: " + item);

                                        boolean itemFound = false;

                                        for (Map<String, Object> orderItem : orderItems) {
                                            if (orderItem.get("item_id").equals(item.get("item_id"))) {
                                                // If the item is found, update the quantity and price

                                                // Safely retrieve and cast the current quantity
                                                Number currentQuantityNumber = (Number) orderItem.get("item_order_quantity");
                                                int currentQuantity = currentQuantityNumber.intValue();

                                                Number newQuantityNumber = (Number) item.get("item_order_quantity");
                                                int newQuantity = currentQuantity + newQuantityNumber.intValue();
                                                orderItem.put("item_order_quantity", newQuantity);

                                                // Safely retrieve and calculate the total price
                                                Number itemPriceNumber = (Number) orderItem.get("item_price");
                                                int itemPrice = itemPriceNumber.intValue();

                                                int newTotalPrice = itemPrice * newQuantity;
                                                orderItem.put("item_total_price", newTotalPrice);

                                                String itemId = (String) orderItem.get("item_id");
                                                if ("Iu8LNnyv7Mq6S1Xz5hTd".equals(itemId) || "Pp4FPWv56jS2cJcWOLlE".equals(itemId)) {
                                                    // Retrieve item_order_quantity and calculate the discount
                                                    Number orderQuantityNumber = (Number) orderItem.get("item_order_quantity");
                                                    int orderQuantity = orderQuantityNumber.intValue();

                                                    int totalDiscount = (orderQuantity / 3) * 5; // Apply a discount of 5 for every 3 items
                                                    newTotalPrice -= totalDiscount; // Deduct the discount from the total price
                                                    orderItem.put("item_total_price", newTotalPrice);
                                                }

                                                itemFound = true;
                                                break; // Break inner loop as the item is already updated
                                            }
                                        }

                                        // If the item is not found, add it to orderItems
                                        if (!itemFound) {
                                            orderItems.add(item);
                                        }
                                    }

                                    // Optionally, calculate the total price of the updated orderItems
                                    int updatedTotalPrice = 0;
                                    for (Map<String, Object> orderItem : orderItems) {
                                        // Safely retrieve and cast the total price
                                        Number totalPriceNumber = (Number) orderItem.get("item_total_price");
                                        updatedTotalPrice += totalPriceNumber.intValue();
                                    }
                                    Log.d("updated order items", "Updated Total Price: " + updatedTotalPrice);

                                    // Update the document with the new list
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("order_items", orderItems);
                                    updates.put("total_amount", updatedTotalPrice);
                                    db.collection("pendingOrders")
                                            .document(document.getId())
                                            .update(updates)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(OrderReceipt.this, "Order updated successfully", Toast.LENGTH_SHORT).show();

                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(OrderReceipt.this, "Failed to update order", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                // No pending order found for the user
                                db.collection("pendingOrders")
                                        .document(documentId)
                                        .set(orderData)
                                        .addOnSuccessListener(aVoid -> {
                                            saveOrderInRealtimeDatabase(documentId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(OrderReceipt.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(OrderReceipt.this, "Error checking pending orders", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OrderReceipt.this, "Failed to fetch pending orders", Toast.LENGTH_SHORT).show();
                    });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(OrderReceipt.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
        });


    }

    // if user order something then
    // save the orderID and order status to 'userUID'
    // if userUID is already exist just append the new data
    private void saveOrderInRealtimeDatabase(String documentId){
        String userUID = mAuth.getCurrentUser().getUid();
        DatabaseReference myRef = rdb.getReference(userUID);

        // Create an order object
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", documentId);
        orderData.put("orderStatus", "ORDERED");

        // Save the order data under 'orders' node
        if (documentId != null) {
            myRef.child("orders").child(documentId).setValue(orderData)
                .addOnSuccessListener(aVoid -> {
                    // Order saved successfully
                    Log.d("Order", "Order saved successfully");
                })
                .addOnFailureListener(e -> {
                    // Failed to save order
                    Log.e("Order", "Failed to save order", e);
                });
        }
    }
}
