package UserHomePageDirectory.HomeFragmentUtils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import UserHomePageDirectory.FragmentsDirectory.HistoryFragment;
import UserHomePageDirectory.MainDashboardUser;
import UserHomePageDirectory.OrderTrackingFragments.UserPendingOrdersFragment;

public class OrderReceipt extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AddedItems addedItems;
    private Map<String, Object> currentDefaultAddress;
    private Map<String, Object> GCashPaymentDetails;
    private String modeOfPayment, additionalMessage;
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
    }

    private void getIntentData(){
        // Retrieve intent data
        Intent intent = getIntent();
        addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        currentDefaultAddress = (Map<String, Object>) intent.getSerializableExtra("deliveryAddress");
        modeOfPayment = (String) intent.getSerializableExtra("modeOfPayment");
        additionalMessage = (String) intent.getSerializableExtra("additionalMessage");

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

        db.collection("pendingOrders")
            .document(documentId)
            .set(orderData)
            .addOnSuccessListener(aVoid -> {
            })
            .addOnFailureListener(e -> {
                Toast.makeText(OrderReceipt.this, "Failed to save order", Toast.LENGTH_SHORT).show();
            });
    }
}
