package UserHomePageDirectory.OrderTrackingFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import AdminHomePageDirectory.Orders.Utils.GCashPaymentDetails;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersAdapter;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;
import UserHomePageDirectory.OrderTrackingUtils.PendingOrders.UserEditPendingOrders;
import UserHomePageDirectory.OrderTrackingUtils.PendingOrders.UserPendingOrdersAdapter;

public class UserPendingOrdersFragment extends Fragment {

    private RecyclerView recyclerViewHolder;

    private List<PendingOrdersConstructor> pendingOrdersConstructorList;
    private UserPendingOrdersAdapter userPendingOrdersAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    public UserPendingOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_pending_orders, container, false);
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            boolean refreshNeeded = data.getBooleanExtra("refreshNeeded", false);
                            if (refreshNeeded) {
                                initializeObjects(view);
                                populatePendingOrdersList();
                            }
                        }
                    }
                }
        );

        initializeObjects(view);
        populatePendingOrdersList();
        return view;
    }

    private void initializeObjects(View view){
        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        pendingOrdersConstructorList = new ArrayList<>();
        userPendingOrdersAdapter = new UserPendingOrdersAdapter(getActivity(), pendingOrdersConstructorList, activityResultLauncher);
        recyclerViewHolder.setAdapter(userPendingOrdersAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void populatePendingOrdersList(){
        String currentUserId = auth.getCurrentUser().getUid();
        db.collection("pendingOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> pendingOrdersList = task.getResult().getDocuments();

                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

                for (DocumentSnapshot document : pendingOrdersList){
                    Log.d("Pending order fragment", "document.getId(): "+document.getId());
                    if (!document.getId().equals("test_id") && document.getString("user_id").equals(currentUserId)) {
                        Log.d("Pending order fragment", "document.getString(\"user_id\"): "+document.getString("user_id"));
                        Log.d("Pending order fragment", "currentUserId: "+currentUserId);
                        Log.d("Pending order fragment", "document: "+document.getData());

                        PendingOrdersConstructor currentPendingOrder = document.toObject(PendingOrdersConstructor.class);
                        if (currentPendingOrder != null){
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
                userPendingOrdersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}