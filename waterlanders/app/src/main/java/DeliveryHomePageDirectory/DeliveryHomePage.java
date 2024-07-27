package DeliveryHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import LoginDirectory.Login;

public class DeliveryHomePage extends AppCompatActivity {

    RecyclerView recyclerView;
    PendingOrderAdapter pendingOrderAdapter;
    List<GetPendingOrder> pendingOrderList;
    FirebaseFirestore db;

    Button logout_button;
    TextView txt_your_deliveries, txt_delivered_orders, txt_available_orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_home_page);

        recyclerView = findViewById(R.id.rv_orderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pendingOrderList = new ArrayList<>();
        pendingOrderAdapter = new PendingOrderAdapter(pendingOrderList, this);
        recyclerView.setAdapter(pendingOrderAdapter);

        txt_your_deliveries = findViewById(R.id.your_deliveries);
        txt_delivered_orders = findViewById(R.id.delivered_orders);
        txt_available_orders = findViewById(R.id.available_orders);
        logout_button = findViewById(R.id.button);

        db = FirebaseFirestore.getInstance();
        getOrdersFromFireStore();

        txt_your_deliveries.setOnClickListener(view -> {
            Toast.makeText(DeliveryHomePage.this, "Clicked your_deliveries", Toast.LENGTH_SHORT).show();
        });

        txt_delivered_orders.setOnClickListener(view -> {
            Toast.makeText(DeliveryHomePage.this, "Clicked delivered_orders", Toast.LENGTH_SHORT).show();
        });

        txt_available_orders.setOnClickListener(view -> {
            Toast.makeText(DeliveryHomePage.this, "Clicked available_orders", Toast.LENGTH_SHORT).show();
        });

        logout_button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(DeliveryHomePage.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeliveryHomePage.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void getOrdersFromFireStore(){
        db.collection("pendingOrders")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.w("DeliveryHomePage", "Listen failed.", error);
                            return;
                        }
                        pendingOrderList.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()){
                            GetPendingOrder items = snapshot.toObject(GetPendingOrder.class);
                            if (items != null) {
                                pendingOrderList.add(items);
                            } else {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(DeliveryHomePage.this, "Logged Out. No orders in database.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(DeliveryHomePage.this, Login.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                        if (pendingOrderAdapter != null) {
                            pendingOrderAdapter.notifyDataSetChanged();
                        } else {
                            pendingOrderAdapter = new PendingOrderAdapter(pendingOrderList, DeliveryHomePage.this);
                            recyclerView.setAdapter(pendingOrderAdapter);
                        }
                    }
                });
    }

}
