package UserHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OrderReceipt extends AppCompatActivity {
    FirebaseFirestore db;
    private AddedItems addedItems;
    private Map<String, Object> currentDefaultAddress;
    private String modeOfPayment, additionalMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_receipt);
        CardView button_btn = findViewById(R.id.button_ok);

        db = FirebaseFirestore.getInstance();

        // Retrieve intent data
        Intent intent = getIntent();
        addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        currentDefaultAddress = (Map<String, Object>) intent.getSerializableExtra("deliveryAddress");
        modeOfPayment = (String) intent.getSerializableExtra("modeOfPayment");
        additionalMessage = (String) intent.getSerializableExtra("additionalMessage");

        // Save order to Firebase
        generateUniqueDocumentId();

        // Set up button and its click listener
        button_btn.setOnClickListener(view -> {
            Intent backToHome = new Intent(OrderReceipt.this, MainDashboardUser.class);
            startActivity(backToHome);
            finish();
        });
    }

    // flow
    // this first generate a random id from 'pendingOrders' collection
    // then it will check if that id is already existed in 'onDelivery' collection
    // this way we can pass the the id from a collection to another without conflict
    // this way as well we can track the order properly without being confused because of changing the id
    // then if the ids are existed then generate again
    // else save the order details to the firebase firestore
    private void generateUniqueDocumentId() {
        String documentId = db.collection("pendingOrders").document().getId();
        checkDocumentInOnDelivery(documentId);
    }

    private void checkDocumentInOnDelivery(String documentId) {
        db.collection("onDelivery")
                .document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().exists()) {
                            // Document ID already exists, generate a new one
                            generateUniqueDocumentId();
                        } else {
                            // Document ID is unique, retrieve item details and save the order
                            saveOrderWithUniqueDocumentId(documentId);
                        }
                    } else {
                        Toast.makeText(OrderReceipt.this, "Failed to check onDelivery collection.", Toast.LENGTH_SHORT).show();
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
        orderData.put("user_id", addedItems.getUserId());
        orderData.put("mode_of_payment", modeOfPayment);
        orderData.put("additional_message", additionalMessage);

        if (modeOfPayment.equals("GCash")){
            orderData.put("reference_number", "put-the-reference-number-here");
            orderData.put("uploaded_receipt", "link-to-uploaded-receipt");
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
