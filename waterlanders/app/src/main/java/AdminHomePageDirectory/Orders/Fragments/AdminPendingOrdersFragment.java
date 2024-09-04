package AdminHomePageDirectory.Orders.Fragments;

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

import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersAdapter;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;

public class AdminPendingOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<PendingOrdersConstructor> pendingOrdersConstructorList;
    private PendingOrdersAdapter pendingOrdersAdapter;

    private FirebaseFirestore db;

    public AdminPendingOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_pending_orders, container, false);
        initializeObjects(view);
        populatePendingOrdersList();

        return view;
    }

    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        pendingOrdersConstructorList = new ArrayList<>();
        pendingOrdersAdapter = new PendingOrdersAdapter(getActivity(), pendingOrdersConstructorList);
        recyclerViewHolder.setAdapter(pendingOrdersAdapter);

        db = FirebaseFirestore.getInstance();
    }

    private void populatePendingOrdersList(){
        db.collection("pendingOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> pendingOrdersList = task.getResult().getDocuments();

                for (DocumentSnapshot document : pendingOrdersList){
                    PendingOrdersConstructor currentPendingOrder = document.toObject(PendingOrdersConstructor.class);
                    if (currentPendingOrder != null){
                        pendingOrdersConstructorList.add(currentPendingOrder);
                    }
                    pendingOrdersAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}