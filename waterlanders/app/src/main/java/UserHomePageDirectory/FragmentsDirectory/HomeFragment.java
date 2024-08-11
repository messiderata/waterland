package UserHomePageDirectory.FragmentsDirectory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import UserHomePageDirectory.AddedItems;
import UserHomePageDirectory.GetItems;
import UserHomePageDirectory.ItemAdapter;
import UserHomePageDirectory.OrderConfirmation;

public class HomeFragment extends Fragment implements ItemAdapter.OnTotalAmountChangeListener {

    private ItemAdapter itemAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private static final String TAG = "UserHomePage";
    private TextView textTotalAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d(TAG, "onCreateView: HomeFragment started");

        RecyclerView recyclerView = view.findViewById(R.id.rv_userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true); // Added for optimization

        itemsList = new ArrayList<>();

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        itemAdapter = new ItemAdapter(itemsList, getContext(), userId);
        itemAdapter.setOnTotalAmountChangeListener(this);
        recyclerView.setAdapter(itemAdapter);

        textTotalAmount = view.findViewById(R.id.total_amount);
        MaterialButton orderButton = view.findViewById(R.id.placeOrderButton);

        db = FirebaseFirestore.getInstance();
        getItemsFromFireStore();
        Log.d(TAG, "itemsList: " + itemsList);

        orderButton.setOnClickListener(v -> {
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

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getItemsFromFireStore() {
        db.collection("items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching data", error);
                        return;
                    }
                    if (value != null) {
                        List<GetItems> newItemsList = new ArrayList<>();
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            GetItems items = snapshot.toObject(GetItems.class);
                            if (items != null) {
                                items.setItem_id(snapshot.getId());
                                Log.d(TAG, "--> items: " + items);
                                newItemsList.add(items);
                            }
                        }
                        itemsList.clear();
                        itemsList.addAll(newItemsList);
                        itemAdapter.notifyDataSetChanged();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTotalAmountChange(int totalAmount, AddedItems addedItems) {
        // Implement your logic to handle total amount change
        textTotalAmount.setText("₱" + totalAmount);

    }
}