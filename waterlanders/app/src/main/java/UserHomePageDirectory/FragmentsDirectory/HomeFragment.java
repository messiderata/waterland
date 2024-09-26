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

import UserHomePageDirectory.HomeFragmentUtils.AddedItems;
import UserHomePageDirectory.HomeFragmentUtils.GetItems;
import UserHomePageDirectory.HomeFragmentUtils.ItemAdapter;
import UserHomePageDirectory.HomeFragmentUtils.OrderConfirmation;

public class HomeFragment extends Fragment implements ItemAdapter.OnTotalAmountChangeListener {

    private RecyclerView recyclerView;
    private MaterialButton orderButton;

    private ItemAdapter itemAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private static final String TAG = "UserHomePage";
    private TextView textTotalAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeObjects(view);
        getItemsFromFireStore();

        // if the order button is clicked
        // then pass all the items and total price
        // to the order confirmation to check if the items
        // and delivery address details as well as payment methods are correct
        orderButton.setOnClickListener(v -> {
            AddedItems addedItems = itemAdapter.getAddedItems();
            addedItems.logCartItems();

            if (!addedItems.getCartItems().isEmpty()) {
                Intent intent = new Intent(getContext(), OrderConfirmation.class);
                intent.putExtra("addedItems", addedItems);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Select an item first.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void initializeObjects(View view){
        recyclerView = view.findViewById(R.id.rv_userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // initialize recycler view and itemAdapter to display the items
        // from the database
        //
        // these adapter is responsible to update the current price
        // of all items selected of the customer
        itemsList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemsList, getContext());
        itemAdapter.setOnTotalAmountChangeListener(this);
        recyclerView.setAdapter(itemAdapter);

        textTotalAmount = view.findViewById(R.id.total_amount);
        orderButton = view.findViewById(R.id.placeOrderButton);
        db = FirebaseFirestore.getInstance();
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
                        if (!snapshot.getId().equals("test_id")){
                            GetItems items = snapshot.toObject(GetItems.class);
                            if (items != null) {
                                items.setItem_id(snapshot.getId());
                                Log.d(TAG, "--> items: " + items);
                                newItemsList.add(items);
                            }
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