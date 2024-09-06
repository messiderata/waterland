package DeliveryHomePageDirectory.DeliveryOrders.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.waterlanders.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import AdminHomePageDirectory.Orders.Utils.DeliveredOrders.DeliveredOrdersConstructor;
import DeliveryHomePageDirectory.DeliveryOrders.Utils.DeliveredOrders.DeliveryDeliveredOrdersAdapter;

public class DeliveryDeliveredOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<DeliveredOrdersConstructor> deliveredOrdersConstructorList;
    private DeliveryDeliveredOrdersAdapter deliveryDeliveredOrdersAdapter;

    private FirebaseFirestore db;

    public DeliveryDeliveredOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_delivered_orders, container, false);
        initializeObjects(view);
        populateDeliveredOrdersList();

        return view;
    }

    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        deliveredOrdersConstructorList = new ArrayList<>();
        deliveryDeliveredOrdersAdapter = new DeliveryDeliveredOrdersAdapter(getActivity(), deliveredOrdersConstructorList);
        recyclerViewHolder.setAdapter(deliveryDeliveredOrdersAdapter);

        db = FirebaseFirestore.getInstance();
    }

    private void populateDeliveredOrdersList(){
        db.collection("deliveredOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> OrdersList = task.getResult().getDocuments();

                for (DocumentSnapshot document : OrdersList){
                    if (!document.getId().equals("test_id")){
                        DeliveredOrdersConstructor currentPendingOrder = document.toObject(DeliveredOrdersConstructor.class);
                        if (currentPendingOrder != null){
                            deliveredOrdersConstructorList.add(currentPendingOrder);
                        }
                    }
                }
                deliveryDeliveredOrdersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}