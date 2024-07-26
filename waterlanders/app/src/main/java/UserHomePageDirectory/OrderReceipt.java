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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_receipt);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        AddedItems addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        saveOrderToFirebase(addedItems);

        button_btn = findViewById(R.id.button);
        button_btn.setOnClickListener(view -> {
            Intent backToHome = new Intent(OrderReceipt.this, UserHomePage.class);
            startActivity(backToHome);
            finish();
        });
    }

    private void saveOrderToFirebase(AddedItems addedItems) {
        generateUniqueDocumentId(addedItems);
    }

    private void generateUniqueDocumentId(AddedItems addedItems) {
        String documentId = db.collection("pendingOrders").document().getId(); // Generate a new document ID
        checkDocumentInOnDelivery(documentId, addedItems);
    }

    private void checkDocumentInOnDelivery(String documentId, AddedItems addedItems) {
        db.collection("onDelivery")
                .document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // Document ID already exists, generate a new one
                            generateUniqueDocumentId(addedItems);
                        } else {
                            // Document ID is unique, retrieve item details and save the order
                            retrieveItemDetailsAndSaveOrder(documentId, addedItems);
                        }
                    } else {
                        Toast.makeText(OrderReceipt.this, "Failed to check onDelivery collection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void retrieveItemDetailsAndSaveOrder(String documentId, AddedItems addedItems) {
        List<Map<String, Object>> orderItems = new ArrayList<>();
        List<String> itemIds = new ArrayList<>(addedItems.getItemIds());

        for (String itemId : itemIds) {
            db.collection("items") // Assuming "items" is the collection name where item details are stored
                    .document(itemId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();
                            Map<String, Object> itemData = new HashMap<>();
                            itemData.put("item_id", document.getId());
                            itemData.put("item_name", document.getString("item_name"));
                            itemData.put("item_img", document.getString("item_img"));
                            itemData.put("item_price", document.getDouble("item_price"));
                            orderItems.add(itemData);

                            if (orderItems.size() == itemIds.size()) {
                                saveOrderWithUniqueDocumentId(documentId, addedItems, orderItems);
                            }
                        } else {
                            Toast.makeText(OrderReceipt.this, "Failed to retrieve item details.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveOrderWithUniqueDocumentId(String documentId, AddedItems addedItems, List<Map<String, Object>> orderItems) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("user_id", addedItems.getUserId());
        orderData.put("order_items", orderItems);
        orderData.put("total_amount", addedItems.getTotalAmount());
        orderData.put("date_order", Timestamp.now());

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