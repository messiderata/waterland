package AdminHomePageDirectory.Orders.Utils.PickupOrders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import AdminHomePageDirectory.Orders.Utils.GCashPaymentDetails;
import AdminHomePageDirectory.Orders.Utils.OnDeliveryOrders.OnDeliveryOrdersConstructor;
import AdminHomePageDirectory.Orders.Utils.OrderedItemsConstructor;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersCurrentOrderDetailsAdapter;

public class PickupOrdersCurrentOrdersDetails extends AppCompatActivity {

    private ImageView backButton;

    private TextView customerName;
    private TextView customerContactNumber;
    private TextView customerID;
    private TextView customerDeliveryAddress;

    private TextView dateOrdered;

    private TextView orderID;
    private TextView deliveryID;

    private RecyclerView recyclerViewHolder;
    private TextView totalOrderAmount;

    private LinearLayout modeOfPaymentContainer;
    private TextView modeOfPayment;

    private TextView orderStatus;

    private TextView additionalMessage;

    private Button backButton2;

    private OnDeliveryOrdersConstructor onDeliveryOrdersConstructor;
    private List<OrderedItemsConstructor> orderedItemsConstructorList;
    private PendingOrdersCurrentOrderDetailsAdapter pendingOrdersCurrentOrderDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_pickup_orders_current_orders_details);

        initializeObjects();
        getIntentData();
        setTextValues();
        populateOrderList();

        // set onclick listener to mode of payment if gcash
        // to check the payment details
        if (String.valueOf(modeOfPayment.getText()).equals("GCash")){
            modeOfPaymentContainer.setOnClickListener(view -> {
                Map<String, Object> gcash_payment_details = onDeliveryOrdersConstructor.getGcash_payment_details();
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
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);

        customerName = findViewById(R.id.customer_name);
        customerContactNumber = findViewById(R.id.customer_contact_number);
        customerID = findViewById(R.id.customer_id);
        customerDeliveryAddress = findViewById(R.id.customer_delivery_address);

        dateOrdered = findViewById(R.id.date_ordered);
        orderID = findViewById(R.id.order_id);
        deliveryID = findViewById(R.id.delivery_id);

        recyclerViewHolder = findViewById(R.id.recycle_view_holder);
        totalOrderAmount = findViewById(R.id.total_order_amount);

        modeOfPaymentContainer = findViewById(R.id.mode_of_payment_container);
        modeOfPayment = findViewById(R.id.mode_of_payment);

        orderStatus = findViewById(R.id.order_status);

        additionalMessage = findViewById(R.id.additional_message);

        backButton2 = findViewById(R.id.back_button_2);

        // for populating the recycler view item list
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(this));
        orderedItemsConstructorList = new ArrayList<>();
        pendingOrdersCurrentOrderDetailsAdapter = new PendingOrdersCurrentOrderDetailsAdapter(this, orderedItemsConstructorList);
        recyclerViewHolder.setAdapter(pendingOrdersCurrentOrderDetailsAdapter);
    }

    private void getIntentData(){
        Intent intent = getIntent();
        onDeliveryOrdersConstructor = intent.getParcelableExtra("current_order");
    }

    private void setTextValues(){
        // address
        Map<String, Object> deliveryAddress = onDeliveryOrdersConstructor.getDelivery_address();
        customerName.setText(String.valueOf(deliveryAddress.get("fullName")));
        customerContactNumber.setText(String.valueOf(deliveryAddress.get("phoneNumber")));
        customerDeliveryAddress.setText(String.valueOf(deliveryAddress.get("deliveryAddress")));

        customerID.setText(String.valueOf(onDeliveryOrdersConstructor.getUser_id()));

        // date ordered
        Timestamp timestamp = onDeliveryOrdersConstructor.getDate_ordered();
        long dateOrderedMillis = timestamp.toDate().getTime();
        Date formatedDateOrdered = new Date(dateOrderedMillis);
        dateOrdered.setText(String.valueOf(formatedDateOrdered));

        // order id
        orderID.setText(String.valueOf(onDeliveryOrdersConstructor.getOrder_id()));
        deliveryID.setText(String.valueOf(onDeliveryOrdersConstructor.getDelivery_id()));

        // total amount
        totalOrderAmount.setText(String.format("Total Amount: ₱" + onDeliveryOrdersConstructor.getTotal_amount()));

        // mode of payment
        modeOfPayment.setText(String.valueOf(onDeliveryOrdersConstructor.getMode_of_payment()));

        // order status
        orderStatus.setText(String.valueOf(onDeliveryOrdersConstructor.getOrder_status()));

        // additional message
        String customerMessage = String.valueOf(onDeliveryOrdersConstructor.getAdditional_message());
        if (customerMessage.isEmpty()){
            additionalMessage.setText(String.format("NONE"));
        } else {
            additionalMessage.setText(customerMessage);
        }
    }

    private void populateOrderList(){
        List<Map<String, Object>> orderItems = onDeliveryOrdersConstructor.getOrder_items();
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
}