package DeliveryHomePageDirectory.onDelivery;

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
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class DetailsOnDeliveryOrders extends AppCompatActivity {
    private TextView orderIDTextView;
    private TextView dateOrderedTextView;
    private TextView userIDTextView;
    private TextView userAddressTextView;
    private TextView deliveryIDTextView;
    private TextView totalAmountTextView;
    private TextInputEditText orderLocationEditText;
    private TextInputLayout orderLocationInputLayout;
    private RecyclerView orderItemsRecyclerView;
    private Button backBtn;
    private Button updateBtn;
    private Button deliveredBtn;

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
        orderLocationInputLayout = findViewById(R.id.text_input_layout_order_location);
        orderLocationEditText = findViewById(R.id.order_current_location);
        orderItemsRecyclerView = findViewById(R.id.rv_orderList);
        backBtn = findViewById(R.id.back_button);
        updateBtn = findViewById(R.id.update_button);
        deliveredBtn = findViewById(R.id.delivered_button);

        // Retrieve data from Intent
        Intent intent = getIntent();
        String curentLocation = intent.getStringExtra("current_location");
        long dateOrderedMillis = intent.getLongExtra("date_ordered", 0);
        Date dateOrdered = new Date(dateOrderedMillis);
        String deliveryID = intent.getStringExtra("delivery_id");
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
        String fmt_deliveryID = "Your Delivery ID: "+ deliveryID;
        String fmt_totalAmount = "Total Amount: ₱"+ totalAmount;
        String fmt_currentLocation = "Previous Location: "+ curentLocation;

        orderIDTextView.setText(fmt_orderID);
        dateOrderedTextView.setText(fmt_dateOrdered);
        userIDTextView.setText(fmt_userID);
        userAddressTextView.setText(fmt_userAddress);
        deliveryIDTextView.setText(fmt_deliveryID);
        totalAmountTextView.setText(fmt_totalAmount);
        orderLocationInputLayout.setHint(fmt_currentLocation);

        // display the orderItems to the orderItemsRecyclerView
        DetailsAdapterOnDeliveryOrders adapter = new DetailsAdapterOnDeliveryOrders(this, orderItems);
        orderItemsRecyclerView.setAdapter(adapter);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backBtn.setOnClickListener(view -> {
            finish();
        });

        updateBtn.setOnClickListener(view -> {
            String orderCurrLocation = String.valueOf(orderLocationEditText.getText());
            if (!TextUtils.isEmpty(orderCurrLocation)){
                Toast.makeText(DetailsOnDeliveryOrders.this, "Location updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailsOnDeliveryOrders.this, "Enter the current order location.", Toast.LENGTH_SHORT).show();
            }
        });

        deliveredBtn.setOnClickListener(view -> {
            String orderCurrLocation = String.valueOf(orderLocationEditText.getText());
            if (!TextUtils.isEmpty(orderCurrLocation)){
                Toast.makeText(DetailsOnDeliveryOrders.this, "Order Status Updated Successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailsOnDeliveryOrders.this, "Enter the current order location.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}