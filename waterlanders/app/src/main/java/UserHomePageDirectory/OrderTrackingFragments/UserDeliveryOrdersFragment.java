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

import AdminHomePageDirectory.Orders.Utils.OnDeliveryOrders.OnDeliveryOrdersAdapter;
import AdminHomePageDirectory.Orders.Utils.OnDeliveryOrders.OnDeliveryOrdersConstructor;
import UserHomePageDirectory.OrderTrackingUtils.DeliveryOrders.UserDeliveryOrdersAdapter;

public class UserDeliveryOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<OnDeliveryOrdersConstructor> onDeliveryOrdersConstructorList;
    private UserDeliveryOrdersAdapter userDeliveryOrdersAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public UserDeliveryOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_delivery_orders, container, false);
        initializeObjects(view);
        populateOnDeliveryOrdersList();

        return view;
    }
    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        onDeliveryOrdersConstructorList = new ArrayList<>();
        userDeliveryOrdersAdapter = new UserDeliveryOrdersAdapter(getActivity(), onDeliveryOrdersConstructorList);
        recyclerViewHolder.setAdapter(userDeliveryOrdersAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void populateOnDeliveryOrdersList(){
        String currentUserId = auth.getCurrentUser().getUid();
        db.collection("onDelivery").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> OrdersList = task.getResult().getDocuments();

                for (DocumentSnapshot document : OrdersList){
                    if (!document.getId().equals("test_id") && document.getString("user_id").equals(currentUserId)){
                        OnDeliveryOrdersConstructor currentPendingOrder = document.toObject(OnDeliveryOrdersConstructor.class);
                        if (currentPendingOrder != null){
                            onDeliveryOrdersConstructorList.add(currentPendingOrder);
                        }
                    }
                }
                userDeliveryOrdersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}