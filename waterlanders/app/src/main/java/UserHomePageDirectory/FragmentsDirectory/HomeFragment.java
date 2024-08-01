package UserHomePageDirectory.FragmentsDirectory;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import UserHomePageDirectory.AddedItems;
import UserHomePageDirectory.GetItems;
import UserHomePageDirectory.ItemAdapter;

public class HomeFragment extends Fragment implements ItemAdapter.OnTotalAmountChangeListener {

    private ItemAdapter itemAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
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

        RecyclerView recyclerView = view.findViewById(R.id.rv_userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true); // Added for optimization

        itemsList = new ArrayList<>();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        itemAdapter = new ItemAdapter(itemsList, getContext(), userId);
        itemAdapter.setOnTotalAmountChangeListener(this);
        recyclerView.setAdapter(itemAdapter);

        db = FirebaseFirestore.getInstance();
        getItemsFromFireStore();
        Log.d(TAG, "itemsList: " + itemsList);

        return view;
    }

    private void getItemsFromFireStore() {
        db.collection("items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
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
                    }
                });
    }

    @Override
    public void onTotalAmountChange(int totalAmount, AddedItems addedItems) {
        // Implement your logic to handle total amount change
    }
}