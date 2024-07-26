package UserHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderReceipt extends AppCompatActivity {
    Button button_btn;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // Ensure this utility is correctly implemented
        setContentView(R.layout.activity_order_receipt);

        db = FirebaseFirestore.getInstance();

        // Retrieve intent data
        Intent intent = getIntent();
        AddedItems addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        String userAddress = intent.getStringExtra("userAddress");

        // Save order to Firebase
        saveOrderToFirebase(addedItems, userAddress);

        // Set up button and its click listener
        button_btn = findViewById(R.id.button);
        button_btn.setOnClickListener(view -> {
            Intent backToHome = new Intent(OrderReceipt.this, UserHomePage.class);
            startActivity(backToHome);
            finish();
        });
    }

    private void saveOrderToFirebase(AddedItems addedItems, String userAddress) {
        generateUniqueDocumentId(addedItems, userAddress);
    }

    private void generateUniqueDocumentId(AddedItems addedItems, String userAddress) {
        String documentId = db.collection("pendingOrders").document().getId(); // Generate a new document ID
        checkDocumentInOnDelivery(documentId, addedItems, userAddress);
    }

    private void checkDocumentInOnDelivery(String documentId, AddedItems addedItems, String userAddress) {
        db.collection("onDelivery")
                .document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().exists()) {
                            // Document ID already exists, generate a new one
                            generateUniqueDocumentId(addedItems, userAddress);
                        } else {
                            // Document ID is unique, retrieve item details and save the order
                            retrieveItemDetailsAndSaveOrder(documentId, addedItems, userAddress);
                        }
                    } else {
                        Toast.makeText(OrderReceipt.this, "Failed to check onDelivery collection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void retrieveItemDetailsAndSaveOrder(String documentId, AddedItems addedItems, String userAddress) {
        List<Map<String, Object>> orderItems = new ArrayList<>();
        List<String> itemIds = new ArrayList<>(addedItems.getItemIds());

        for (String itemId : itemIds) {
            db.collection("items") // Assuming "items" is the collection name where item details are stored
                    .document(itemId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();
                            Map<String, Object> itemData = new HashMap<>();
                            itemData.put("item_id", document.getId());
                            itemData.put("item_name", document.getString("item_name"));
                            itemData.put("item_img", document.getString("item_img"));
                            itemData.put("item_price", document.getDouble("item_price"));
                            orderItems.add(itemData);

                            if (orderItems.size() == itemIds.size()) {
                                String orderIcon = document.getString("item_img");
                                saveOrderWithUniqueDocumentId(documentId, addedItems, orderItems, userAddress, orderIcon);
                            }
                        } else {
                            Toast.makeText(OrderReceipt.this, "Failed to retrieve item details.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveOrderWithUniqueDocumentId(String documentId, AddedItems addedItems, List<Map<String, Object>> orderItems, String userAddress, String orderIcon) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("user_id", addedItems.getUserId());
        orderData.put("order_items", orderItems);
        orderData.put("total_amount", addedItems.getTotalAmount());
        orderData.put("date_ordered", Timestamp.now());
        orderData.put("user_address", userAddress);
        orderData.put("order_icon", orderIcon);
        orderData.put("order_id", documentId);

        db.collection("pendingOrders")
                .document(documentId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OrderReceipt.this, "Order saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrderReceipt.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                });
    }
}
