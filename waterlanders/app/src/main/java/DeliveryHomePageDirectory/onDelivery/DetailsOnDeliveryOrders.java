package DeliveryHomePageDirectory.onDelivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import DeliveryHomePageDirectory.PendingOrders.DetailsPendingOrders;


public class DetailsOnDeliveryOrders extends AppCompatActivity {
    private TextView orderIDTextView;
    private TextView dateOrderedTextView;
    private TextView userIDTextView;
    private TextView userAddressTextView;
    private TextView deliveryIDTextView;
    private TextView totalAmountTextView;
    private RadioGroup orderStatusRadioGroup;
    private RadioButton orderStatus1RadioButton;
    private RadioButton orderStatus2RadioButton;
    private RadioButton orderStatus3RadioButton;
    private RecyclerView orderItemsRecyclerView;
    private Button backBtn;
    private Button updateBtn;
    private Button deliveredBtn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_delivery_orders_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderIDTextView = findViewById(R.id.orderID);
        dateOrderedTextView = findViewById(R.id.dateOrdered);
        userIDTextView = findViewById(R.id.userID);
        userAddressTextView = findViewById(R.id.userAddress);
        deliveryIDTextView = findViewById(R.id.deliveryID);
        totalAmountTextView = findViewById(R.id.totalAmount);
        orderStatusRadioGroup = findViewById(R.id.order_status_group);
        orderStatus1RadioButton = findViewById(R.id.order_current_status_1);
        orderStatus2RadioButton = findViewById(R.id.order_current_status_2);
        orderStatus3RadioButton = findViewById(R.id.order_current_status_3);
        orderItemsRecyclerView = findViewById(R.id.rv_orderList);
        backBtn = findViewById(R.id.back_button);
        updateBtn = findViewById(R.id.update_button);
        deliveredBtn = findViewById(R.id.delivered_button);
        db = FirebaseFirestore.getInstance();

        // Retrieve data from Intent
        Intent intent = getIntent();
        long dateOrderedMillis = intent.getLongExtra("date_ordered", 0);
        Date dateOrdered = new Date(dateOrderedMillis);
        String deliveryID = intent.getStringExtra("delivery_id");
        String orderIcon = intent.getStringExtra("order_icon");
        String orderID = intent.getStringExtra("order_id");
        ArrayList<Map<String, Object>> orderItems = (ArrayList<Map<String, Object>>) intent.getSerializableExtra("order_items");
        String orderStatus = intent.getStringExtra("order_status");
        int totalAmount = intent.getIntExtra("total_amount", 0);
        String userAddress = intent.getStringExtra("user_address");
        String userID = intent.getStringExtra("user_id");

        // format strings
        String fmt_orderID = "Order ID: "+ orderID;
        String fmt_dateOrdered = "Date Ordered: "+ dateOrdered;
        String fmt_userID = "User ID: "+ userID;
        String fmt_userAddress = "User Address: "+ userAddress;
        String fmt_deliveryID = "Your Delivery ID: "+ deliveryID;
        String fmt_totalAmount = "Total Amount: ₱"+ totalAmount;

        orderIDTextView.setText(fmt_orderID);
        dateOrderedTextView.setText(fmt_dateOrdered);
        userIDTextView.setText(fmt_userID);
        userAddressTextView.setText(fmt_userAddress);
        deliveryIDTextView.setText(fmt_deliveryID);
        totalAmountTextView.setText(fmt_totalAmount);

        // Check the appropriate RadioButton based on the 'orderStatus'
        setOrderStatus(orderStatus);

        // display the orderItems to the orderItemsRecyclerView
        DetailsAdapterOnDeliveryOrders adapter = new DetailsAdapterOnDeliveryOrders(this, orderItems);
        orderItemsRecyclerView.setAdapter(adapter);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backBtn.setOnClickListener(view -> {
            finish();
        });

        updateBtn.setOnClickListener(view -> {
            String orderCurrStatus = ((RadioButton) findViewById(orderStatusRadioGroup.getCheckedRadioButtonId())).getText().toString();
            if (!TextUtils.isEmpty(orderCurrStatus)){
                if (orderCurrStatus.equals(orderStatus3RadioButton.getText().toString())){
                    Toast.makeText(DetailsOnDeliveryOrders.this, "Click 'DELIVERED' button instead.", Toast.LENGTH_SHORT).show();
                } else {
                    db.collection("onDelivery")
                            .document(orderID)
                            .update("order_status", orderCurrStatus)
                            .addOnSuccessListener(aVoid -> Toast.makeText(DetailsOnDeliveryOrders.this, "Location updated successfully.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(DetailsOnDeliveryOrders.this, "Failed to update location.", Toast.LENGTH_SHORT).show());
                    finish();
                }
            } else {
                Toast.makeText(DetailsOnDeliveryOrders.this, "Enter the updated location.", Toast.LENGTH_SHORT).show();
            }
        });

        deliveredBtn.setOnClickListener(view -> {
            String orderCurrStatus = ((RadioButton) findViewById(orderStatusRadioGroup.getCheckedRadioButtonId())).getText().toString();
            if (!TextUtils.isEmpty(orderCurrStatus)){
                if (orderCurrStatus.equals(orderStatus3RadioButton.getText().toString())){
                    Map<String, Object> deliveredOrder = new HashMap<>();
                    deliveredOrder.put("date_delivered", Timestamp.now());
                    deliveredOrder.put("date_ordered", dateOrdered);
                    deliveredOrder.put("delivery_id", deliveryID);
                    deliveredOrder.put("order_icon", orderIcon);
                    deliveredOrder.put("order_id", orderID);
                    deliveredOrder.put("order_items", orderItems);
                    deliveredOrder.put("order_status", orderCurrStatus);
                    deliveredOrder.put("total_amount", totalAmount);
                    deliveredOrder.put("user_address", userAddress);
                    deliveredOrder.put("user_confirmation", "PENDING");
                    deliveredOrder.put("user_id", userID);

                    // Save the data to the 'deliveredOrders' collection
                    db.collection("deliveredOrders")
                            .document(orderID)
                            .set(deliveredOrder)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(DetailsOnDeliveryOrders.this, "Order saved successfully!", Toast.LENGTH_SHORT).show();
                                // Remove the data from 'pendingOrders' collection
                                db.collection("onDelivery")
                                        .document(orderID)
                                        .delete()
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(DetailsOnDeliveryOrders.this, "Order removed from pending orders", Toast.LENGTH_SHORT).show();
                                            finish(); // Close the activity
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(DetailsOnDeliveryOrders.this, "Failed to remove order from pending orders", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(DetailsOnDeliveryOrders.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(DetailsOnDeliveryOrders.this, "Check 'DELIVERED' option first.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DetailsOnDeliveryOrders.this, "Enter the current status of the order.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setOrderStatus(String orderStatus) {
        switch (orderStatus) {
            case "PREPARING":
                orderStatus1RadioButton.setChecked(true);
                break;
            case "INTRANSIT":
                orderStatus2RadioButton.setChecked(true);
                break;
            case "DELIVERED":
                orderStatus3RadioButton.setChecked(true);
                break;
            default:
                orderStatus1RadioButton.setChecked(false);
                orderStatus2RadioButton.setChecked(false);
                orderStatus3RadioButton.setChecked(false);
                break;
        }
    }
}