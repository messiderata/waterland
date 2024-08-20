package DeliveryHomePageDirectory.deliveredOrders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class DetailsDeliveredOrders extends AppCompatActivity {
    private TextView orderIDTextView;
    private TextView dateOrderedTextView;
    private TextView userIDTextView;
    private TextView userAddressTextView;
    private TextView deliveryIDTextView;
    private TextView orderStatusTextView;
    private TextView dateDeliveredTextView;
    private TextView customerConfirmationTextView;
    private TextView totalAmountTextView;
    private RecyclerView orderItemsRecyclerView;
    private Button okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details_delivered_orders);
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
        orderStatusTextView = findViewById(R.id.orderStatus);
        dateDeliveredTextView = findViewById(R.id.dateDelivered);
        customerConfirmationTextView = findViewById(R.id.userConfirmation);
        totalAmountTextView = findViewById(R.id.totalAmount);
        orderItemsRecyclerView = findViewById(R.id.rv_orderList);
        okBtn = findViewById(R.id.button_ok);

        // Retrieve data from Intent
        Intent intent = getIntent();
        long dateDeliveredMillis = intent.getLongExtra("date_delivered", 0);
        Date dateDelivered = new Date(dateDeliveredMillis);
        long dateOrderedMillis = intent.getLongExtra("date_ordered", 0);
        Date dateOrdered = new Date(dateOrderedMillis);
        String deliveryID = intent.getStringExtra("delivery_id");
        String orderIcon = intent.getStringExtra("order_icon");
        String orderID = intent.getStringExtra("order_id");
        ArrayList<Map<String, Object>> orderItems = (ArrayList<Map<String, Object>>) intent.getSerializableExtra("order_items");
        String orderStatus = intent.getStringExtra("order_status");
        int totalAmount = intent.getIntExtra("total_amount", 0);
        String userAddress = intent.getStringExtra("user_address");
        String userConfirmation = intent.getStringExtra("user_confirmation");
        String userID = intent.getStringExtra("user_id");

        // format strings
        String fmt_orderID = "Order ID: "+ orderID;
        String fmt_dateOrdered = "Date Ordered: "+ dateOrdered;
        String fmt_userID = "User ID: "+ userID;
        String fmt_userAddress = "User Address: "+ userAddress;
        String fmt_deliveryID = "Your Delivery ID: "+ deliveryID;
        String fmt_orderStatus = "Order Status: "+ orderStatus;
        String fmt_dateDelivered = "Date Delivered: "+ dateDelivered;
        String fmt_customerConfirmation = "Customer Confirmation: "+ userConfirmation;
        String fmt_totalAmount = "Total Amount: ₱"+ totalAmount;

        orderIDTextView.setText(fmt_orderID);
        dateOrderedTextView.setText(fmt_dateOrdered);
        userIDTextView.setText(fmt_userID);
        userAddressTextView.setText(fmt_userAddress);
        deliveryIDTextView.setText(fmt_deliveryID);
        orderStatusTextView.setText(fmt_orderStatus);
        dateDeliveredTextView.setText(fmt_dateDelivered);
        customerConfirmationTextView.setText(fmt_customerConfirmation);
        totalAmountTextView.setText(fmt_totalAmount);

        // display the orderItems to the orderItemsRecyclerView
        DetailsAdapterDeliveredOrders adapter = new DetailsAdapterDeliveredOrders(this, orderItems);
        orderItemsRecyclerView.setAdapter(adapter);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        okBtn.setOnClickListener(view -> {
            finish();
        });
    }
}