package UserHomePageDirectory.OrderTrackingUtils.Completed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import AdminHomePageDirectory.Orders.Utils.DeliveredOrders.DeliveredOrdersConstructor;
import AdminHomePageDirectory.Orders.Utils.DeliveredOrders.DeliveredOrdersProofOfDelivery;
import AdminHomePageDirectory.Orders.Utils.GCashPaymentDetails;
import AdminHomePageDirectory.Orders.Utils.OrderedItemsConstructor;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersCurrentOrderDetailsAdapter;

public class UserCompletedOrdersCurrentOrderDetails extends AppCompatActivity {

    private ImageView backButton;

    private TextView customerName;
    private TextView customerContactNumber;
    private TextView accountStatus;
    private TextView customerID;
    private TextView customerDeliveryAddress;

    private TextView dateOrdered;
    private TextView dateDelivered;

    private TextView orderID;
    private TextView deliveryID;

    private RecyclerView recyclerViewHolder;
    private TextView totalOrderAmount;

    private LinearLayout modeOfPaymentContainer;
    private TextView modeOfPayment;
    private TextView isPaid;

    private TextView orderStatus;

    private TextView additionalMessage;

    private LinearLayout proofOfDeliveryContainer;

    private TextView userConfirmation;
    private TextView customerFeedback;
    private TextView editFeedback;

    private Button backButton2;

    private DeliveredOrdersConstructor deliveredOrdersConstructor;
    private List<OrderedItemsConstructor> orderedItemsConstructorList;
    private PendingOrdersCurrentOrderDetailsAdapter pendingOrdersCurrentOrderDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_completed_orders_current_order_details);
        initializeObjects();
        getIntentData();
        setTextValues();
        populateOrderList();

        // set onclick listener to mode of payment if gcash
        // to check the payment details
        if (String.valueOf(modeOfPayment.getText()).equals("GCash")){
            modeOfPaymentContainer.setOnClickListener(view -> {
                Map<String, Object> gcash_payment_details = deliveredOrdersConstructor.getGcash_payment_details();
                Intent showGCashPaymentIntent = new Intent(this, GCashPaymentDetails.class);
                showGCashPaymentIntent.putExtra("gcash_payment_details", (Serializable) gcash_payment_details);
                startActivity(showGCashPaymentIntent);
            });
        }

        // set onclick listener to proof of delivery
        // to check the delivery picture
        proofOfDeliveryContainer.setOnClickListener(view -> {
            String proofOfDeliveryLink = deliveredOrdersConstructor.getProof_of_delivery();
            Intent showproofOfDeliveryIntent = new Intent(this, DeliveredOrdersProofOfDelivery.class);
            showproofOfDeliveryIntent.putExtra("proof_of_delivery_link", proofOfDeliveryLink);
            startActivity(showproofOfDeliveryIntent);
        });

        editFeedback.setOnClickListener(view -> {
            Intent editReviewIntent = new Intent(this, UserCompletedOrdersEditReview.class);
            editReviewIntent.putExtra("order_status", deliveredOrdersConstructor.getOrder_status());
            editReviewIntent.putExtra("order_id", deliveredOrdersConstructor.getOrder_id());
            editReviewIntent.putExtra("customer_feedback", deliveredOrdersConstructor.getCustomer_feedback());
            startActivity(editReviewIntent);
        });

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
        accountStatus = findViewById(R.id.account_status);

        dateOrdered = findViewById(R.id.date_ordered);
        dateDelivered = findViewById(R.id.date_delivered);

        orderID = findViewById(R.id.order_id);
        deliveryID = findViewById(R.id.delivery_id);

        recyclerViewHolder = findViewById(R.id.recycle_view_holder);
        totalOrderAmount = findViewById(R.id.total_order_amount);

        modeOfPaymentContainer = findViewById(R.id.mode_of_payment_container);
        modeOfPayment = findViewById(R.id.mode_of_payment);
        isPaid = findViewById(R.id.is_paid);

        orderStatus = findViewById(R.id.order_status);

        additionalMessage = findViewById(R.id.additional_message);

        proofOfDeliveryContainer = findViewById(R.id.proof_of_delivery_container);

        userConfirmation = findViewById(R.id.user_confirmation);
        customerFeedback = findViewById(R.id.customer_feedback);
        editFeedback = findViewById(R.id.edit_review);

        backButton2 = findViewById(R.id.back_button_2);

        // for populating the recycler view item list
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(this));
        orderedItemsConstructorList = new ArrayList<>();
        pendingOrdersCurrentOrderDetailsAdapter = new PendingOrdersCurrentOrderDetailsAdapter(this, orderedItemsConstructorList);
        recyclerViewHolder.setAdapter(pendingOrdersCurrentOrderDetailsAdapter);
    }

    private void getIntentData(){
        Intent intent = getIntent();
        deliveredOrdersConstructor = intent.getParcelableExtra("current_order");
    }

    private void setTextValues(){
        // address
        Map<String, Object> deliveryAddress = deliveredOrdersConstructor.getDelivery_address();
        customerName.setText(String.valueOf(deliveryAddress.get("fullName")));
        customerContactNumber.setText(String.valueOf(deliveryAddress.get("phoneNumber")));
        customerDeliveryAddress.setText(String.valueOf(deliveryAddress.get("deliveryAddress")));

        customerID.setText(String.valueOf(deliveredOrdersConstructor.getUser_id()));
        accountStatus.setText(String.valueOf(deliveredOrdersConstructor.getAccountStatus()));

        // date ordered
        Timestamp timestamp = deliveredOrdersConstructor.getDate_ordered();
        long dateOrderedMillis = timestamp.toDate().getTime();
        Date formatedDateOrdered = new Date(dateOrderedMillis);
        dateOrdered.setText(String.valueOf(formatedDateOrdered));

        // date delivered
        Timestamp timestampDelivered = deliveredOrdersConstructor.getDate_delivered();
        long dateDeliveredMillis = timestampDelivered.toDate().getTime();
        Date formatedDateDelivered = new Date(dateDeliveredMillis);
        dateDelivered.setText(String.valueOf(formatedDateDelivered));

        // order id
        orderID.setText(String.valueOf(deliveredOrdersConstructor.getOrder_id()));
        deliveryID.setText(String.valueOf(deliveredOrdersConstructor.getDelivery_id()));

        // total amount
        totalOrderAmount.setText(String.format("Total Amount: ₱" + deliveredOrdersConstructor.getTotal_amount()));

        // mode of payment
        modeOfPayment.setText(String.valueOf(deliveredOrdersConstructor.getMode_of_payment()));
        isPaid.setText(String.valueOf(deliveredOrdersConstructor.getIsPaid()));

        // order status
        orderStatus.setText(String.valueOf(deliveredOrdersConstructor.getOrder_status()));

        // additional message
        String customerMessage = String.valueOf(deliveredOrdersConstructor.getAdditional_message());
        if (customerMessage.isEmpty()){
            additionalMessage.setText(String.format("NONE"));
        } else {
            additionalMessage.setText(customerMessage);
        }

        // user confirmation
        userConfirmation.setText(String.valueOf(deliveredOrdersConstructor.getUser_confirmation()));

        // customer feedback
        customerFeedback.setText(String.valueOf(deliveredOrdersConstructor.getCustomer_feedback()));
    }

    private void populateOrderList(){
        List<Map<String, Object>> orderItems = deliveredOrdersConstructor.getOrder_items();
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