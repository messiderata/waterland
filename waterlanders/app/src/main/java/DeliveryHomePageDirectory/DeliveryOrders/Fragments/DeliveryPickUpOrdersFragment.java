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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import AdminHomePageDirectory.Orders.Utils.OnDeliveryOrders.OnDeliveryOrdersConstructor;
import DeliveryHomePageDirectory.DeliveryOrders.Utils.OnDeliveryOrders.DeliveryOnDeliveryOrdersAdapter;
import DeliveryHomePageDirectory.DeliveryOrders.Utils.PickupOrders.DeliveryPickupOrdersAdapter;

public class DeliveryPickUpOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<OnDeliveryOrdersConstructor> onDeliveryOrdersConstructorList;
    private DeliveryPickupOrdersAdapter deliveryOnDeliveryOrdersAdapter;

    private FirebaseFirestore db;

    public DeliveryPickUpOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_pick_up_orders, container, false);
        initializeObjects(view);
        populateOnDeliveryOrdersList();
        return view;
    }

    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        onDeliveryOrdersConstructorList = new ArrayList<>();
        deliveryOnDeliveryOrdersAdapter = new DeliveryPickupOrdersAdapter(getActivity(), onDeliveryOrdersConstructorList);
        recyclerViewHolder.setAdapter(deliveryOnDeliveryOrdersAdapter);

        db = FirebaseFirestore.getInstance();
    }

    private void populateOnDeliveryOrdersList(){
        db.collection("pickupOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> OrdersList = task.getResult().getDocuments();

                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

                for (DocumentSnapshot document : OrdersList){
                    if (!document.getId().equals("test_id")){
                        OnDeliveryOrdersConstructor currentPendingOrder = document.toObject(OnDeliveryOrdersConstructor.class);
                        if (currentPendingOrder != null){
                            // Convert date_ordered to the formatted string
                            Timestamp dateOrdered = currentPendingOrder.getDate_ordered();  // Assuming this is a Timestamp
                            if (dateOrdered != null) {
                                ZonedDateTime zonedDateTime = dateOrdered.toDate().toInstant()
                                        .atZone(ZoneId.of("GMT+08:00"));  // Adjust time zone if needed
                                String formattedDate = zonedDateTime.format(outputFormatter);
                                currentPendingOrder.setFormattedDateOrdered(formattedDate);  // Assuming you have a setter for this
                            }
                            onDeliveryOrdersConstructorList.add(currentPendingOrder);
                        }
                    }
                }
                // Sort the list based on 'date_delivery' in ascending order
                onDeliveryOrdersConstructorList.sort((o1, o2) -> {
                    String date1 = o1.getDate_delivery();
                    String date2 = o2.getDate_delivery();

                    // Handle null cases: place null dates at the end
                    if (date1 == null && date2 == null) return 0;
                    if (date1 == null) return 1;  // place o1 after o2
                    if (date2 == null) return -1; // place o1 before o2

                    // Compare non-null dates lexicographically
                    return date1.compareTo(date2);
                });
                deliveryOnDeliveryOrdersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}