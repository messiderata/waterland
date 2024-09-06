package DeliveryHomePageDirectory.DeliveryFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeliveryDashboardFragment extends Fragment {

    private TextView totalWaitingOrders;
    private TextView totalOnDeliveryOrders;
    private TextView totalDeliveredOrders;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public DeliveryDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_dashboard, container, false);
        initializeObjects(view);

        // setting values for each cards
        setWaitingOrdersValue();
        setOnDeliveryOrdersValue();
        setDeliveredOrdersValue();

        return view;
    }

    private void initializeObjects(View view){
        totalWaitingOrders = view.findViewById(R.id.total_waiting_orders);
        totalOnDeliveryOrders = view.findViewById(R.id.total_onDelivery_orders);
        totalDeliveredOrders = view.findViewById(R.id.total_delivered_orders);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setWaitingOrdersValue(){
        // get all waiting orders under 'waitingForCourier' collection in the firebase
        // do not display and count that has 'test_id' document id
        db.collection("waitingForCourier")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int count = 0;
                    for (DocumentSnapshot document : task.getResult()) {
                        if (!document.getId().equals("test_id")) {
                            count++;
                        }
                    }
                    totalWaitingOrders.setText(String.valueOf(count));
                } else {
                    totalWaitingOrders.setText("0");
                }
            });
    }

    private void setOnDeliveryOrdersValue(){
        // get onDelivery orders under 'onDelivery' collection in the firebase
        // display and count only the orders if the 'delivery_id' field is same as the
        // user id of the current user
        // do not display and count that has 'test_id' document id
        String currentUserId = auth.getCurrentUser().getUid();
        db.collection("onDelivery")
            .whereEqualTo("delivery_id", currentUserId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int count = 0;
                    for (DocumentSnapshot document : task.getResult()) {
                        if (!document.getId().equals("test_id")) {
                            count++;
                        }
                    }
                    totalOnDeliveryOrders.setText(String.valueOf(count));
                } else {
                    totalOnDeliveryOrders.setText("0");
                }
            });
    }

    private void setDeliveredOrdersValue(){
        // get delivered orders under 'deliveredOrders' collection in the firebase
        // display and count only the orders if the 'delivery_id' field is same as the
        // user id of the current user
        // do not display and count that has 'test_id' document id
        String currentUserId = auth.getCurrentUser().getUid();
        db.collection("deliveredOrders")
            .whereEqualTo("delivery_id", currentUserId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int count = 0;
                    for (DocumentSnapshot document : task.getResult()) {
                        if (!document.getId().equals("test_id")) {
                            count++;
                        }
                    }
                    totalDeliveredOrders.setText(String.valueOf(count));
                } else {
                    totalDeliveredOrders.setText("0");
                }
            });
    }
}