package UserHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Handler.StatusBarUtil;

public class OrderConfirmation extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrdersAdapter ordersAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private Button proceed_btn;
    private ImageView back_btn;
    private TextInputEditText edt_user_address;
    private TextView edt_item_total_price;
    private MaterialCardView gCash_btn, cashOnDelivery;
    private boolean isGcashSelected = false;
    private boolean isPaymentMethodSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);

        Intent intent = getIntent();
        AddedItems addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        Log.d("CartManager", "orderConfirmation");
        addedItems.logCartItems();

        recyclerView = findViewById(R.id.rv_order_confirm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        gCash_btn = findViewById(R.id.G_cash_button);
        cashOnDelivery = findViewById(R.id.Cash_on_delivery_button);

        itemsList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(itemsList, this);
        recyclerView.setAdapter(ordersAdapter);

        back_btn = findViewById(R.id.btn_back);
        proceed_btn = findViewById(R.id.btn_proceed);

        db = FirebaseFirestore.getInstance();
        showCurrentOrders(addedItems);

        String userAddress = "Test";
        edt_item_total_price = findViewById(R.id.itemTotalPrice);

        // display item total price
        String itemTotalPriceFmt = "₱" + addedItems.getTotalAmount();
        edt_item_total_price.setText(itemTotalPriceFmt);

        // Set the click listeners for the MaterialCardView buttons
        gCash_btn.setOnClickListener(view -> {
            togglePaymentMethod(gCash_btn, cashOnDelivery);
        });

        cashOnDelivery.setOnClickListener(view -> {
            togglePaymentMethod(cashOnDelivery, gCash_btn);
        });

        back_btn.setOnClickListener(view -> {
            Intent backIntent = new Intent(OrderConfirmation.this, MainDashboardUser.class);
            startActivity(backIntent);
            finish();
        });
        proceed_btn.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(userAddress)) {
                if(gCash_btn.isChecked() || cashOnDelivery.isChecked()){
                    if (isGcashSelected) {
//                     Navigate to the Gcash confirmation screen
                        Intent proceedIntent = new Intent(OrderConfirmation.this, GcashConfirmation.class);
                        proceedIntent.putExtra("addedItems", addedItems);
                        proceedIntent.putExtra("userAddress", userAddress);
                        startActivity(proceedIntent);
                    } else {
                        // Navigate to the Cash on Delivery confirmation screen
                        Intent proceedIntent = new Intent(OrderConfirmation.this, OrderReceipt.class);
                        proceedIntent.putExtra("addedItems", addedItems);
                        proceedIntent.putExtra("userAddress", userAddress);
                        startActivity(proceedIntent);
                    }
                    finish();
                }else{
                    Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(OrderConfirmation.this, "Enter your delivery address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePaymentMethod(MaterialCardView selectedCard, MaterialCardView otherCard) {
        selectedCard.setChecked(!selectedCard.isChecked());

        if (selectedCard.isChecked()) {
            otherCard.setChecked(false);

            // Update the payment method based on the selected card
            isGcashSelected = selectedCard == gCash_btn;
        }

    }

    private void showCurrentOrders(AddedItems addedItems) {
        List<Map<String, Object>> cartItems = addedItems.getCartItems();
        for (Map<String, Object> itemMap : cartItems) {
            GetItems item = mapToGetItems(itemMap);
            itemsList.add(item);
        }
        ordersAdapter.notifyDataSetChanged();
    }

    private GetItems mapToGetItems(Map<String, Object> map) {
        String itemId = (String) map.get("item_id");
        String itemImg = (String) map.get("item_img");
        String itemName = (String) map.get("item_name");
        Integer itemPrice = (Integer) map.get("item_price");
        Integer itemOrderQuantity = (Integer) map.get("item_order_quantity");
        Integer itemTotalPrice = (Integer) map.get("item_total_price");

        return new GetItems(itemName, itemPrice, itemImg, itemOrderQuantity, itemTotalPrice);
    }
}
