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

import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;
import DeliveryHomePageDirectory.DeliveryOrders.Utils.WaitingOrders.DeliveryWaitingOrdersAdapter;

public class DeliveryWaitingOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<PendingOrdersConstructor> pendingOrdersConstructorList;
    private DeliveryWaitingOrdersAdapter deliveryWaitingOrdersAdapter;

    private FirebaseFirestore db;

    public DeliveryWaitingOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_waiting_orders, container, false);
        initializeObjects(view);
        populateWaitingOrdersList();

        return view;
    }

    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        pendingOrdersConstructorList = new ArrayList<>();
        deliveryWaitingOrdersAdapter = new DeliveryWaitingOrdersAdapter(getActivity(), pendingOrdersConstructorList);
        recyclerViewHolder.setAdapter(deliveryWaitingOrdersAdapter);

        db = FirebaseFirestore.getInstance();
    }

    private void populateWaitingOrdersList(){
        db.collection("waitingForCourier").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> pendingOrdersList = task.getResult().getDocuments();

                for (DocumentSnapshot document : pendingOrdersList){
                    if (!document.getId().equals("test_id")){
                        PendingOrdersConstructor currentPendingOrder = document.toObject(PendingOrdersConstructor.class);
                        if (currentPendingOrder != null){
                            pendingOrdersConstructorList.add(currentPendingOrder);
                        }
                    }
                }
                deliveryWaitingOrdersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}