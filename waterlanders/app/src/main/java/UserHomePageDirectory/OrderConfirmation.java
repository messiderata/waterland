package UserHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class OrderConfirmation extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrdersAdapter ordersAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private TextView textTotalAmount;
    private Button proceed_btn;
    private ImageView back_btn;
    private TextInputEditText edt_user_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        Intent intent = getIntent();
        AddedItems addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        Log.d("CartManager", "oerderConfirmation");
        addedItems.logCartItems();

        recyclerView = findViewById(R.id.rv_order_confirm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemsList = new ArrayList<>();

        ordersAdapter = new OrdersAdapter(itemsList, this);
        recyclerView.setAdapter(ordersAdapter);

        back_btn = findViewById(R.id.btn_back);
        proceed_btn = findViewById(R.id.btn_proceed);

        db = FirebaseFirestore.getInstance();
        showCurrentOrders(addedItems);

        edt_user_address = findViewById(R.id.user_address);

        back_btn.setOnClickListener(view -> {
            Intent backIntent = new Intent(OrderConfirmation.this, MainDashboardUser.class);
            startActivity(backIntent);
            finish();
        });

        proceed_btn.setOnClickListener(view -> {
            // for now lets save the data to the database
            // regardless of the payment method lets add that later
            // para may progress kahit papano

            String userAddress = String.valueOf(edt_user_address.getText());
            if (!TextUtils.isEmpty(userAddress)){
                Toast.makeText(OrderConfirmation.this, "Order Success", Toast.LENGTH_SHORT).show();
                Intent proceedIntent = new Intent(OrderConfirmation.this, OrderReceipt.class);
                proceedIntent.putExtra("addedItems", addedItems);
                proceedIntent.putExtra("userAddress", userAddress);
                // pass the value of edt_user_address as well
                startActivity(proceedIntent);
                finish();
            } else {
                Toast.makeText(OrderConfirmation.this, "Enter your delivery address.", Toast.LENGTH_SHORT).show();
            }
        });

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
        // Create a new GetItems object and populate it using the map data
        String itemId = (String) map.get("item_id");
        String itemImg = (String) map.get("item_img");
        String itemName = (String) map.get("item_name");
        Integer itemPrice = (Integer) map.get("item_price");
        Integer itemOrderQuantity = (Integer) map.get("item_order_quantity");
        Integer itemTotalPrice = (Integer) map.get("item_total_price");

        // Assuming GetItems has a constructor or setters for these fields
        return new GetItems(itemName, itemPrice, itemImg, itemOrderQuantity, itemTotalPrice);
    }

}