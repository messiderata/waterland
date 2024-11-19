package DeliveryHomePageDirectory.DeliveryOrders.Utils.WaitingOrders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import AdminHomePageDirectory.Orders.Utils.GCashPaymentDetails;
import AdminHomePageDirectory.Orders.Utils.OrderedItemsConstructor;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersCurrentOrderDetailsAdapter;
import AdminHomePageDirectory.SuccessScreen;
import DeliveryHomePageDirectory.DeliveryOrders.Utils.DeliverySuccessScreen;

public class DeliveryWaitingOrdersCurrentOrderDetails extends AppCompatActivity {

    private ImageView backButton;

    private TextView customerName;
    private TextView customerContactNumber;
    private TextView customerID;
    private TextView customerDeliveryAddress;

    private TextView dateOrdered;

    private TextView orderID;

    private RecyclerView recyclerViewHolder;
    private TextView totalOrderAmount;

    private LinearLayout modeOfPaymentContainer;
    private TextView modeOfPayment;

    private TextView orderStatus;

    private TextView additionalMessage;

    private Button backButton2;
    private Button saveButton;

    private PendingOrdersConstructor pendingOrdersConstructor;
    private List<OrderedItemsConstructor> orderedItemsConstructorList;
    private PendingOrdersCurrentOrderDetailsAdapter pendingOrdersCurrentOrderDetailsAdapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseDatabase rdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_waiting_orders_current_order_details);
        initializeObjects();
        getIntentData();
        setTextValues();
        populateOrderList();

        // set onclick listener to mode of payment if gcash
        // to check the payment details
        if (String.valueOf(modeOfPayment.getText()).equals("GCash")){
            modeOfPaymentContainer.setOnClickListener(view -> {
                Map<String, Object> gcash_payment_details = pendingOrdersConstructor.getGcash_payment_details();
                Intent showGCashPaymentIntent = new Intent(this, GCashPaymentDetails.class);
                showGCashPaymentIntent.putExtra("gcash_payment_details", (Serializable) gcash_payment_details);
                startActivity(showGCashPaymentIntent);
            });
        }

        // other buttons
        backButton.setOnClickListener(v -> {
            finish();
        });

        backButton2.setOnClickListener(v -> {
            finish();
        });

        saveButton.setOnClickListener(v -> {
            // update the 'order_status' to 'ON DELIVERY'
            // add 'delivery_id' with the value of the current courier delivery id
            // then save the updated order to the 'onDelivery' collection
            // then remove the order to the 'waitingForCourier' collection
            transferOrder();
        });
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);

        customerName = findViewById(R.id.customer_name);
        customerContactNumber = findViewById(R.id.customer_contact_number);
        customerID = findViewById(R.id.customer_id);
        customerDeliveryAddress = findViewById(R.id.customer_delivery_address);

        dateOrdered = findViewById(R.id.date_ordered);
        orderID = findViewById(R.id.order_id);

        recyclerViewHolder = findViewById(R.id.recycle_view_holder);
        totalOrderAmount = findViewById(R.id.total_order_amount);

        modeOfPaymentContainer = findViewById(R.id.mode_of_payment_container);
        modeOfPayment = findViewById(R.id.mode_of_payment);

        orderStatus = findViewById(R.id.order_status);

        additionalMessage = findViewById(R.id.additional_message);

        backButton2 = findViewById(R.id.back_button_2);
        saveButton = findViewById(R.id.save_button);

        // for populating the recycler view item list
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(this));
        orderedItemsConstructorList = new ArrayList<>();
        pendingOrdersCurrentOrderDetailsAdapter = new PendingOrdersCurrentOrderDetailsAdapter(this, orderedItemsConstructorList);
        recyclerViewHolder.setAdapter(pendingOrdersCurrentOrderDetailsAdapter);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        rdb = FirebaseDatabase.getInstance();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        pendingOrdersConstructor = intent.getParcelableExtra("current_order");
    }

    private void setTextValues(){
        // address
        Map<String, Object> deliveryAddress = pendingOrdersConstructor.getDelivery_address();
        customerName.setText(String.valueOf(deliveryAddress.get("fullName")));
        customerContactNumber.setText(String.valueOf(deliveryAddress.get("phoneNumber")));
        customerDeliveryAddress.setText(String.valueOf(deliveryAddress.get("deliveryAddress")));

        customerID.setText(String.valueOf(pendingOrdersConstructor.getUser_id()));

        // date ordered
        Timestamp timestamp = pendingOrdersConstructor.getDate_ordered();
        long dateOrderedMillis = timestamp.toDate().getTime();
        Date formatedDateOrdered = new Date(dateOrderedMillis);
        dateOrdered.setText(String.valueOf(formatedDateOrdered));

        // order id
        orderID.setText(String.valueOf(pendingOrdersConstructor.getOrder_id()));

        // total amount
        totalOrderAmount.setText(String.format("Total Amount: ₱" + pendingOrdersConstructor.getTotal_amount()));

        // mode of payment
        modeOfPayment.setText(String.valueOf(pendingOrdersConstructor.getMode_of_payment()));

        // order status
        orderStatus.setText(String.valueOf(pendingOrdersConstructor.getOrder_status()));

        // additional message
        String customerMessage = String.valueOf(pendingOrdersConstructor.getAdditional_message());
        if (customerMessage.isEmpty()){
            additionalMessage.setText(String.format("NONE"));
        } else {
            additionalMessage.setText(customerMessage);
        }
    }

    private void populateOrderList(){
        List<Map<String, Object>> orderItems = pendingOrdersConstructor.getOrder_items();
        for (Map<String, Object> item : orderItems){
            // Extract the item details from the map
            String itemId = (String) item.get("item_id");
            String itemImg = (String) item.get("item_img");
            String itemName = (String) item.get("item_name");
            int itemOrderQuantity = ((Number) item.get("item_order_quantity")).intValue();
            int itemPrice = ((Number) item.get("item_price")).intValue();
            int itemTotalPrice = ((Number) item.get("item_total_price")).intValue();

            // Create a new OrderedItemsConstructor object
            OrderedItemsConstructor orderedItem = new OrderedItemsConstructor(
                    itemId, itemImg, itemName, itemOrderQuantity, itemPrice, itemTotalPrice
            );

            // Add the object to the list
            orderedItemsConstructorList.add(orderedItem);
        }
        pendingOrdersCurrentOrderDetailsAdapter.notifyDataSetChanged();
    }

    private void transferOrder(){
        String orderId = pendingOrdersConstructor.getOrder_id();
        String deliveryId = auth.getCurrentUser().getUid();

        // Create a map with the current order data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("additional_message", pendingOrdersConstructor.getAdditional_message());
        orderData.put("date_delivery", pendingOrdersConstructor.getDate_delivery());
        orderData.put("date_ordered", pendingOrdersConstructor.getDate_ordered());
        orderData.put("delivery_address", pendingOrdersConstructor.getDelivery_address());
        orderData.put("delivery_id", deliveryId);
        orderData.put("mode_of_payment", pendingOrdersConstructor.getMode_of_payment());
        orderData.put("order_icon", pendingOrdersConstructor.getOrder_icon());
        orderData.put("order_id", pendingOrdersConstructor.getOrder_id());
        orderData.put("order_items", pendingOrdersConstructor.getOrder_items());
        orderData.put("order_status", "ON DELIVERY");
        orderData.put("search_term", pendingOrdersConstructor.getSearch_term());
        orderData.put("total_amount", pendingOrdersConstructor.getTotal_amount());
        orderData.put("user_id", pendingOrdersConstructor.getUser_id());

        if (String.valueOf(modeOfPayment.getText()).equals("GCash")){
            orderData.put("gcash_payment_details", pendingOrdersConstructor.getGcash_payment_details());
        }

        db.collection("waitingForCourier").document(orderId).delete()
            .addOnSuccessListener(aVoid -> {
                db.collection("onDelivery").document(orderId).set(orderData)
                    .addOnSuccessListener(aVoid1 -> {
                        updateOrderStatusInRealtimeDatabase("ON DELIVERY");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to move order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete order from waitingForCourier: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateOrderStatusInRealtimeDatabase(String orderStatus){
        String orderId = pendingOrdersConstructor.getOrder_id();
        String userId = pendingOrdersConstructor.getUser_id();
        DatabaseReference myRef = rdb.getReference(userId).child("orders").child(orderId);

        // Create a map with the updated status
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("orderStatus", orderStatus);

        // Update the order data
        myRef.updateChildren(updateData)
            .addOnSuccessListener(aVoid -> {
                // Order updated successfully
                Intent showSucessScreenIntent = new Intent(this, DeliverySuccessScreen.class);
                showSucessScreenIntent.putExtra("success_message", "TAKE ORDER\n" + "SUCCESSFUL");
                showSucessScreenIntent.putExtra("success_description", "order has been moved to your \n" + "‘ON DELIVERY’ tab");
                showSucessScreenIntent.putExtra("fragment", "orders");
                startActivity(showSucessScreenIntent);
                finish();
            })
            .addOnFailureListener(e -> {
                // Failed to update order
                Log.e("Order", "Failed to update order", e);
            });
    }
}