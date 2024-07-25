package UserHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import LoginDirectory.Login;

public class OrderConfirmation extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrdersAdapter ordersAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private TextView textTotalAmount;
    private Button logout_button, back_btn, proceed_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_confirmation);

        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        AddedItems addedItems = (AddedItems) intent.get().getSerializableExtra("addedItems");

        recyclerView = findViewById(R.id.rv_order_confirm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemsList = new ArrayList<>();

        ordersAdapter = new OrdersAdapter(itemsList, this);
        recyclerView.setAdapter(ordersAdapter);

        back_btn = findViewById(R.id.btn_back);
        proceed_btn = findViewById(R.id.btn_proceed);
        logout_button = findViewById(R.id.button);

        db = FirebaseFirestore.getInstance();
        showCurrentOrders(addedItems);

        back_btn.setOnClickListener(view -> {
            intent.set(new Intent(OrderConfirmation.this, UserHomePage.class));
            startActivity(intent.get());
            finish();
        });

        proceed_btn.setOnClickListener(view -> {
            Toast.makeText(OrderConfirmation.this, "Order Success", Toast.LENGTH_SHORT).show();
            intent.set(new Intent(OrderConfirmation.this, UserHomePage.class));
            startActivity(intent.get());
            finish();
        });

        logout_button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(OrderConfirmation.this, "Logged Out", Toast.LENGTH_SHORT).show();
            intent.set(new Intent(OrderConfirmation.this, Login.class));
            startActivity(intent.get());
            finish();
        });
    }

    private void showCurrentOrders(AddedItems addedItems) {
        List<String> itemIds = new ArrayList<>(addedItems.getItemIds());
        db.collection("items").whereIn(FieldPath.documentId(), itemIds).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        itemsList.clear();
                        for (DocumentSnapshot snapshot : task.getResult()) {
                            GetItems item = snapshot.toObject(GetItems.class);
                            if (item != null) {
                                item.setItem_id(snapshot.getId());
                                itemsList.add(item);
                            }
                        }
                        ordersAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(OrderConfirmation.this, "There is an error processsing your order.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}