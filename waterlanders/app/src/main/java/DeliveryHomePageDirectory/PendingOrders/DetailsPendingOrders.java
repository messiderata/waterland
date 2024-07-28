package DeliveryHomePageDirectory.PendingOrders;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



public class DetailsPendingOrders extends AppCompatActivity {
    private TextView orderIDTextView;
    private TextView dateOrderedTextView;
    private TextView userIDTextView;
    private TextView userAddressTextView;
    private TextView totalAmountTextView;
    private TextInputEditText orderStatusEditText;
    private RecyclerView orderItemsRecyclerView;
    private Button cancelBtn;
    private Button takeOrderBtn;
    private FirebaseFirestore db;

    private static final String TAG = "PendingOrderDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pending_orders_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderIDTextView = findViewById(R.id.orderID);
        dateOrderedTextView = findViewById(R.id.dateOrdered);
        userIDTextView = findViewById(R.id.userID);
        userAddressTextView = findViewById(R.id.userAddress);
        totalAmountTextView = findViewById(R.id.totalAmount);
        orderStatusEditText = findViewById(R.id.order_current_status);
        orderItemsRecyclerView = findViewById(R.id.rv_orderList);
        cancelBtn = findViewById(R.id.cancel_button);
        takeOrderBtn = findViewById(R.id.takerOrder_button);
        db = FirebaseFirestore.getInstance();

        // Retrieve data from Intent
        Intent intent = getIntent();
        long dateOrderedMillis = intent.getLongExtra("date_ordered", 0);
        Date dateOrdered = new Date(dateOrderedMillis);
        String orderIcon = intent.getStringExtra("order_icon");
        String orderID = intent.getStringExtra("order_id");
        ArrayList<Map<String, Object>> orderItems = (ArrayList<Map<String, Object>>) intent.getSerializableExtra("order_items");
        int totalAmount = intent.getIntExtra("total_amount", 0);
        String userAddress = intent.getStringExtra("user_address");
        String userID = intent.getStringExtra("user_id");

        // format strings
        String fmt_orderID = "Order ID: "+ orderID;
        String fmt_dateOrdered = "Date Ordered: "+ dateOrdered;
        String fmt_userID = "User ID: "+ userID;
        String fmt_userAddress = "User Address: "+ userAddress;
        String fmt_totalAmount = "Total Amount: ₱"+ totalAmount;

        orderIDTextView.setText(fmt_orderID);
        dateOrderedTextView.setText(fmt_dateOrdered);
        userIDTextView.setText(fmt_userID);
        userAddressTextView.setText(fmt_userAddress);
        totalAmountTextView.setText(fmt_totalAmount);

        // display the orderItems to the orderItemsRecyclerView
        DetailsAdapterPendingOrders adapter = new DetailsAdapterPendingOrders(this, orderItems);
        orderItemsRecyclerView.setAdapter(adapter);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        cancelBtn.setOnClickListener(view -> {
            finish();
        });

        takeOrderBtn.setOnClickListener(view -> {
            String orderCurrStatus = String.valueOf(orderStatusEditText.getText());
            if (!TextUtils.isEmpty(orderCurrStatus)){
                Map<String, Object> onDeliveryData = new HashMap<>();
                String deliveryID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                onDeliveryData.put("date_ordered", dateOrdered);
                onDeliveryData.put("delivery_id", deliveryID);
                onDeliveryData.put("order_icon", orderIcon);
                onDeliveryData.put("order_id", orderID);
                onDeliveryData.put("order_items", orderItems);
                onDeliveryData.put("order_status", orderCurrStatus);
                onDeliveryData.put("total_amount", totalAmount);
                onDeliveryData.put("user_address", userAddress);
                onDeliveryData.put("user_id", userID);

                // Save the data to the 'onDelivery' collection
                db.collection("onDelivery")
                        .document(orderID)
                        .set(onDeliveryData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(DetailsPendingOrders.this, "Order saved successfully!", Toast.LENGTH_SHORT).show();
                            // Remove the data from 'pendingOrders' collection
                            db.collection("pendingOrders")
                                    .document(orderID)
                                    .delete()
                                    .addOnSuccessListener(aVoid1 -> {
                                        Toast.makeText(DetailsPendingOrders.this, "Order removed from pending orders", Toast.LENGTH_SHORT).show();
                                        finish(); // Close the activity
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(DetailsPendingOrders.this, "Failed to remove order from pending orders", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(DetailsPendingOrders.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(DetailsPendingOrders.this, "Enter the current order location.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}