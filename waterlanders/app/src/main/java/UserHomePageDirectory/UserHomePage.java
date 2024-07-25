package UserHomePageDirectory;

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

import com.google.firebase.auth.FirebaseAuth;
import com.example.waterlanders.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import LoginDirectory.Login;

public class UserHomePage extends AppCompatActivity implements ItemAdapter.OnTotalAmountChangeListener {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private TextView textTotalAmount;
    private Button logout_button, cancel_btn, purchase_order_btn;

    private static final String TAG = "UserHomePage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_home_page);

        Log.d(TAG, "onCreate: UserHomePage Activity started");

        recyclerView = findViewById(R.id.rv_userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemsList = new ArrayList<>();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        itemAdapter = new ItemAdapter(itemsList, this, userId);
        itemAdapter.setOnTotalAmountChangeListener(this);
        recyclerView.setAdapter(itemAdapter);

        textTotalAmount = findViewById(R.id.text_total_amount);
        cancel_btn = findViewById(R.id.btn_cancel);
        purchase_order_btn = findViewById(R.id.btn_purchase_order);
        logout_button = findViewById(R.id.button);

        db = FirebaseFirestore.getInstance();
        getItemsFromFireStore();
        Log.d(TAG, "itemsList: "+ itemsList);


        cancel_btn.setOnClickListener(view -> {
            Toast.makeText(UserHomePage.this, "EWAN KO KUNG BAKIT MAY CANCEL BUTTON HAHAHAHAHA", Toast.LENGTH_SHORT).show();
        });

        purchase_order_btn.setOnClickListener(view -> {
            AddedItems addedItems = itemAdapter.getAddedItems();
            Log.d(TAG, "-->>> Added Items: " + addedItems.getItemIds());
            Log.d(TAG, "--> Total Amount: " + addedItems.getTotalAmount());

            if (!addedItems.getItemIds().isEmpty()){
                Intent intent = new Intent(UserHomePage.this, OrderConfirmation.class);
                intent.putExtra("addedItems", addedItems);
                startActivity(intent);
            } else {
                Toast.makeText(UserHomePage.this, "Select an item first.", Toast.LENGTH_SHORT).show();
            }

        });

        logout_button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(UserHomePage.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserHomePage.this, Login.class);
            startActivity(intent);
            finish();
        });

    }    // init obj

    private void getItemsFromFireStore(){
        db.collection("items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            return;
                        }
                        itemsList.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()){
                            GetItems items = snapshot.toObject(GetItems.class);
                            if (items != null) {
                                items.setItem_id(snapshot.getId());
                                Log.d(TAG, "--> items: "+ items);
                                itemsList.add(items);
                            }
                        }
                        itemAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onTotalAmountChange(int totalAmount, AddedItems addedItems) {
        textTotalAmount.setText("₱" + totalAmount);
    }
}
