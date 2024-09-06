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

import AdminHomePageDirectory.Orders.Utils.OnDeliveryOrders.OnDeliveryOrdersAdapter;
import AdminHomePageDirectory.Orders.Utils.OnDeliveryOrders.OnDeliveryOrdersConstructor;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;
import DeliveryHomePageDirectory.DeliveryOrders.Utils.OnDeliveryOrders.DeliveryOnDeliveryOrdersAdapter;
import DeliveryHomePageDirectory.DeliveryOrders.Utils.WaitingOrders.DeliveryWaitingOrdersAdapter;

public class DeliveryOnDeliveryOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<OnDeliveryOrdersConstructor> onDeliveryOrdersConstructorList;
    private DeliveryOnDeliveryOrdersAdapter deliveryOnDeliveryOrdersAdapter;

    private FirebaseFirestore db;

    public DeliveryOnDeliveryOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_on_delivery_orders, container, false);
        initializeObjects(view);
        populateOnDeliveryOrdersList();

        return view;
    }

    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        onDeliveryOrdersConstructorList = new ArrayList<>();
        deliveryOnDeliveryOrdersAdapter = new DeliveryOnDeliveryOrdersAdapter(getActivity(), onDeliveryOrdersConstructorList);
        recyclerViewHolder.setAdapter(deliveryOnDeliveryOrdersAdapter);

        db = FirebaseFirestore.getInstance();
    }

    private void populateOnDeliveryOrdersList(){
        db.collection("onDelivery").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> OrdersList = task.getResult().getDocuments();

                for (DocumentSnapshot document : OrdersList){
                    if (!document.getId().equals("test_id")){
                        OnDeliveryOrdersConstructor currentPendingOrder = document.toObject(OnDeliveryOrdersConstructor.class);
                        if (currentPendingOrder != null){
                            onDeliveryOrdersConstructorList.add(currentPendingOrder);
                        }
                    }
                }
                deliveryOnDeliveryOrdersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}