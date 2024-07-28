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

import DeliveryHomePageDirectory.PendingOrders.GetPendingOrder;
import DeliveryHomePageDirectory.PendingOrders.AdapterPendingOrders;
import DeliveryHomePageDirectory.deliveredOrders.AdapterDeliveredOrders;
import DeliveryHomePageDirectory.deliveredOrders.GetDeliveredOrders;
import DeliveryHomePageDirectory.onDelivery.AdapterOnDeliveryOrders;
import DeliveryHomePageDirectory.onDelivery.GetOnDeliveryOrders;
import LoginDirectory.Login;

public class DeliveryHomePage extends AppCompatActivity {

    RecyclerView recyclerView;

    AdapterOnDeliveryOrders OnDeliveryOrdersAdapter;
    List<GetOnDeliveryOrders> OnDeliveryOrdersList;

    AdapterDeliveredOrders deliveredOrdersAdapter;
    List<GetDeliveredOrders> deliveredOrdersList;

    AdapterPendingOrders adapterPendingOrders;
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

        txt_your_deliveries = findViewById(R.id.your_deliveries);
        txt_delivered_orders = findViewById(R.id.delivered_orders);
        txt_available_orders = findViewById(R.id.available_orders);
        logout_button = findViewById(R.id.button);

        db = FirebaseFirestore.getInstance();

        OnDeliveryOrdersList = new ArrayList<>();
        OnDeliveryOrdersAdapter = new AdapterOnDeliveryOrders(OnDeliveryOrdersList, this);
        recyclerView.setAdapter(OnDeliveryOrdersAdapter);
        getDeliveriesFromFireStore();

        txt_your_deliveries.setOnClickListener(view -> {
            OnDeliveryOrdersList = new ArrayList<>();
            OnDeliveryOrdersAdapter = new AdapterOnDeliveryOrders(OnDeliveryOrdersList, this);
            recyclerView.setAdapter(OnDeliveryOrdersAdapter);
            getDeliveriesFromFireStore();
        });

        txt_delivered_orders.setOnClickListener(view -> {
            deliveredOrdersList = new ArrayList<>();
            deliveredOrdersAdapter = new AdapterDeliveredOrders(deliveredOrdersList, this);
            recyclerView.setAdapter(deliveredOrdersAdapter);
            getDeliveredOrdersFromFireStore();
        });

        txt_available_orders.setOnClickListener(view -> {
            pendingOrderList = new ArrayList<>();
            adapterPendingOrders = new AdapterPendingOrders(pendingOrderList, this);
            recyclerView.setAdapter(adapterPendingOrders);
            getOrdersFromFireStore();
        });

        logout_button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(DeliveryHomePage.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeliveryHomePage.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void getDeliveriesFromFireStore(){
        db.collection("onDelivery")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.w("DeliveryHomePage", "Listen failed.", error);
                            return;
                        }
                        OnDeliveryOrdersList.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()){
                            GetOnDeliveryOrders items = snapshot.toObject(GetOnDeliveryOrders.class);
                            if (items != null) {
                                OnDeliveryOrdersList.add(items);
                            }
                        }
                        if (OnDeliveryOrdersAdapter != null) {
                            OnDeliveryOrdersAdapter.notifyDataSetChanged();
                        } else {
                            OnDeliveryOrdersAdapter = new AdapterOnDeliveryOrders(OnDeliveryOrdersList, DeliveryHomePage.this);
                            recyclerView.setAdapter(OnDeliveryOrdersAdapter);
                        }
                    }
                });
    }

    private void getDeliveredOrdersFromFireStore(){
        db.collection("deliveredOrders")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.w("DeliveryHomePage", "Listen failed.", error);
                            return;
                        }
                        deliveredOrdersList.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()){
                            GetDeliveredOrders items = snapshot.toObject(GetDeliveredOrders.class);
                            if (items != null) {
                                deliveredOrdersList.add(items);
                            }
                        }
                        if (deliveredOrdersAdapter != null) {
                            deliveredOrdersAdapter.notifyDataSetChanged();
                        } else {
                            deliveredOrdersAdapter = new AdapterDeliveredOrders(deliveredOrdersList, DeliveryHomePage.this);
                            recyclerView.setAdapter(deliveredOrdersAdapter);
                        }
                    }
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
                            }
                        }
                        if (adapterPendingOrders != null) {
                            adapterPendingOrders.notifyDataSetChanged();
                        } else {
                            adapterPendingOrders = new AdapterPendingOrders(pendingOrderList, DeliveryHomePage.this);
                            recyclerView.setAdapter(adapterPendingOrders);
                        }
                    }
                });
    }

}
