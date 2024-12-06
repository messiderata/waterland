package UserHomePageDirectory.OrderTrackingUtils.PendingOrders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import UserHomePageDirectory.HomeFragmentUtils.AddedItems;
import UserHomePageDirectory.HomeFragmentUtils.GetItems;
import UserHomePageDirectory.HomeFragmentUtils.ItemAdapter;
import UserHomePageDirectory.HomeFragmentUtils.OrderConfirmation;
import UserHomePageDirectory.HomeFragmentUtils.OrderReceipt;

public class UserEditPendingOrders extends AppCompatActivity implements ItemAdapter.OnTotalAmountChangeListener {

    private ImageView backButton;
    private RecyclerView productList;
    private TextView totalAmount;
    private MaterialButton placeOrderButton;

    private ItemAdapter itemAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseDatabase rdb;
    private String orderID;

    private static final String TAG = "edit user order";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_edit_pending_orders);
        initializeObjects();
        getIntentData();
        getItemsFromFireStore();

        placeOrderButton.setOnClickListener(v -> {
            AddedItems addedItems = itemAdapter.getAddedItems();
            addedItems.logCartItems();

            if (!addedItems.getCartItems().isEmpty()) {
                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("order_items", addedItems.getCartItems());
                updatedData.put("total_amount", addedItems.getTotalAmount());

                db.collection("pendingOrders")
                        .document(orderID)
                        .update(updatedData)
                        .addOnSuccessListener(aVoid -> {
                            saveOrderInRealtimeDatabase(orderID);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save order", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Select an item first.", Toast.LENGTH_LONG).show();
            }
        });

        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("refreshNeeded", true);
            resultIntent.putExtra("document_id", orderID);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);
        totalAmount = findViewById(R.id.total_amount);
        placeOrderButton = findViewById(R.id.placeOrderButton);

        productList = findViewById(R.id.rv_userList);
        productList.setLayoutManager(new LinearLayoutManager(this));
        productList.setHasFixedSize(true);

        itemsList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemsList, this);
        itemAdapter.setOnTotalAmountChangeListener(this);
        productList.setAdapter(itemAdapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        rdb = FirebaseDatabase.getInstance();
    }

    private void getIntentData(){
        Intent intentData = getIntent();
        orderID = intentData.getStringExtra("order_id");
        Log.d(TAG, "orderID: "+orderID);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getItemsFromFireStore() {
        db.collection("items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching data", error);
                        return;
                    }
                    if (value != null) {
                        List<GetItems> newItemsList = new ArrayList<>();
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            if (!snapshot.getId().equals("test_id")){
                                GetItems items = snapshot.toObject(GetItems.class);
                                if (items != null) {
                                    items.setItem_id(snapshot.getId());
                                    Log.d(TAG, "--> items: " + items);
                                    newItemsList.add(items);
                                }
                            }
                        }
                        itemsList.clear();
                        itemsList.addAll(newItemsList);
                        itemAdapter.notifyDataSetChanged();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTotalAmountChange(int totalAmountInt, AddedItems addedItems) {
        // Implement your logic to handle total amount change
        totalAmount.setText("₱" + totalAmountInt);
    }

    public void saveOrderInRealtimeDatabase(String documentId){
        String userUID = mAuth.getCurrentUser().getUid();
        DatabaseReference myRef = rdb.getReference(userUID);

        // Create an order object
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", documentId);
        orderData.put("orderStatus", "ORDERED");

        // Save the order data under 'orders' node
        Log.d(TAG, "documentId: "+documentId);
        if (documentId != null) {
            myRef.child("orders").child(documentId).setValue(orderData)
                    .addOnSuccessListener(aVoid -> {
                        // Order saved successfully
                        Log.d(TAG, "Order saved successfully");
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("refreshNeeded", true);
                        resultIntent.putExtra("document_id", orderID);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to save order
                        Log.e(TAG, "Failed to save order", e);
                    });
        }
    }
}