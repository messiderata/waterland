package UserHomePageDirectory.FragmentsDirectory;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import android.util.Log;

import LoginDirectory.Login;
import UserHomePageDirectory.AddedItems;
import UserHomePageDirectory.GetItems;
import UserHomePageDirectory.ItemAdapter;
import UserHomePageDirectory.OrderConfirmation;

public class HomeFragment extends Fragment implements ItemAdapter.OnTotalAmountChangeListener {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private TextView textTotalAmount;
    private Button purchase_order_btn, logout_btn;

    private static final String TAG = "UserHomePage";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Apply window insets to update padding for API level Q and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.setOnApplyWindowInsetsListener((v, insets) -> {
                v.setPadding(v.getPaddingLeft(), insets.getSystemGestureInsets().top,
                        v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }

        Log.d(TAG, "onCreateView: HomeFragment started");

        recyclerView = view.findViewById(R.id.rv_userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemsList = new ArrayList<>();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        itemAdapter = new ItemAdapter(itemsList, getContext(), userId);
        itemAdapter.setOnTotalAmountChangeListener(this);
        recyclerView.setAdapter(itemAdapter);

        textTotalAmount = view.findViewById(R.id.text_total_amount);
        purchase_order_btn = view.findViewById(R.id.btn_purchase_order);
        logout_btn = view.findViewById(R.id.button);

        db = FirebaseFirestore.getInstance();
        getItemsFromFireStore();
        Log.d(TAG, "itemsList: "+ itemsList);

        purchase_order_btn.setOnClickListener(v -> {
            AddedItems addedItems = itemAdapter.getAddedItems();
            Log.d(TAG, "-->>> Added Items: " + addedItems.getItemIds());
            Log.d(TAG, "--> Total Amount: " + addedItems.getTotalAmount());

            if (!addedItems.getItemIds().isEmpty()) {
                Intent intent = new Intent(getContext(), OrderConfirmation.class);
                intent.putExtra("addedItems", addedItems);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Select an item first.", Toast.LENGTH_SHORT).show();
            }
        });

        logout_btn.setOnClickListener(view1 -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logout successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), Login.class);
            startActivity(intent);
        });

        return view;
    }

    private void getItemsFromFireStore() {
        db.collection("items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        itemsList.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            GetItems items = snapshot.toObject(GetItems.class);
                            if (items != null) {
                                items.setItem_id(snapshot.getId());
                                Log.d(TAG, "--> items: " + items);
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
