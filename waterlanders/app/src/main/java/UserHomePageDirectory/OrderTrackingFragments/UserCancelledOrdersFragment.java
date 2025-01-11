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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import AdminHomePageDirectory.Orders.Utils.CancelOrders.CancelOrdersAdapter;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;

public class UserCancelledOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<PendingOrdersConstructor> pendingOrdersConstructorList;
    private CancelOrdersAdapter cancelOrdersAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    public UserCancelledOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_cancelled_orders, container, false);
        initializeObjects(view);
        populatePendingOrdersList();
        return view;
    }

    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        pendingOrdersConstructorList = new ArrayList<>();
        cancelOrdersAdapter = new CancelOrdersAdapter(getActivity(), pendingOrdersConstructorList);
        recyclerViewHolder.setAdapter(cancelOrdersAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
    }

    private void populatePendingOrdersList(){
        db.collection("cancelledOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> pendingOrdersList = task.getResult().getDocuments();

                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

                for (DocumentSnapshot document : pendingOrdersList){
                    if (!document.getId().equals("test_id")){
                        PendingOrdersConstructor currentPendingOrder = document.toObject(PendingOrdersConstructor.class);
                        if (currentPendingOrder != null && currentPendingOrder.getUser_id().equals(userId)){
                            // Convert date_ordered to the formatted string
                            Timestamp dateOrdered = currentPendingOrder.getDate_ordered();  // Assuming this is a Timestamp
                            if (dateOrdered != null) {
                                ZonedDateTime zonedDateTime = dateOrdered.toDate().toInstant()
                                        .atZone(ZoneId.of("GMT+08:00"));  // Adjust time zone if needed
                                String formattedDate = zonedDateTime.format(outputFormatter);
                                currentPendingOrder.setFormattedDateOrdered(formattedDate);  // Assuming you have a setter for this
                            }
                            pendingOrdersConstructorList.add(currentPendingOrder);
                        }
                    }
                }
                // Sort the list based on 'date_delivery' in ascending order
                pendingOrdersConstructorList.sort((o1, o2) -> {
                    String date1 = o1.getDate_delivery();
                    String date2 = o2.getDate_delivery();

                    // Handle null cases: place null dates at the end
                    if (date1 == null && date2 == null) return 0;
                    if (date1 == null) return 1;  // place o1 after o2
                    if (date2 == null) return -1; // place o1 before o2

                    // Compare non-null dates lexicographically
                    return date1.compareTo(date2);
                });
                cancelOrdersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}