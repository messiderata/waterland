package UserHomePageDirectory.OrderTrackingFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersAdapter;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;
import UserHomePageDirectory.OrderTrackingUtils.PendingOrders.UserPendingOrdersAdapter;

public class UserPendingOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<PendingOrdersConstructor> pendingOrdersConstructorList;
    private UserPendingOrdersAdapter userPendingOrdersAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public UserPendingOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_pending_orders, container, false);
        initializeObjects(view);
        populatePendingOrdersList();

        return view;
    }

    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        pendingOrdersConstructorList = new ArrayList<>();
        userPendingOrdersAdapter = new UserPendingOrdersAdapter(getActivity(), pendingOrdersConstructorList);
        recyclerViewHolder.setAdapter(userPendingOrdersAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void populatePendingOrdersList(){
        String currentUserId = auth.getCurrentUser().getUid();
        db.collection("pendingOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> pendingOrdersList = task.getResult().getDocuments();

                for (DocumentSnapshot document : pendingOrdersList){
                    if (!document.getId().equals("test_id") && document.getString("user_id").equals(currentUserId)) {
                        PendingOrdersConstructor currentPendingOrder = document.toObject(PendingOrdersConstructor.class);
                        if (currentPendingOrder != null){
                            pendingOrdersConstructorList.add(currentPendingOrder);
                        }
                    }
                }
                userPendingOrdersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}