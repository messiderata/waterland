package UserHomePageDirectory.FragmentsDirectory;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import LoginDirectory.Login;


public class ProfileFragment extends Fragment {

    private TextView username;
    private TextView totalDeliveredOrders;
    private TextView totalPendingOrders;
    private CardView logOutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeObjects(view);
        getDataFromFirebase();
        setLogOutButton();

        return view;
    }

    private void initializeObjects(View view){
        username = view.findViewById(R.id.user_username);
        totalDeliveredOrders = view.findViewById(R.id.total_delivered_orders);
        totalPendingOrders = view.findViewById(R.id.total_pending_orders);
        logOutButton = view.findViewById(R.id.logout_button_settings);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void getDataFromFirebase(){
        String userID = mAuth.getCurrentUser().getUid();

        // Set the 'username' based on the 'fullName' field
        db.collection("users")
            .document(userID)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String fullName = task.getResult().getString("fullName");
                    if (fullName != null) {
                        username.setText(fullName);
                    }
                } else {
                    Log.e("Profile Fragment", task.toString());
                }
            });

        // Get the total number of delivered orders for the current userId
        db.collection("deliveredOrders")
            .whereEqualTo("user_id", userID)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    int deliveredOrderCount = task.getResult().size();
                    totalDeliveredOrders.setText(String.valueOf(deliveredOrderCount));
                } else {
                    Log.e("Profile Fragment", task.toString());
                }
            });

        // Get the total number of pending orders for the current userId
        db.collection("pendingOrders")
            .whereEqualTo("user_id", userID)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    int pendingOrderCount = task.getResult().size();
                    totalPendingOrders.setText(String.valueOf(pendingOrderCount));
                } else {
                    Log.e("Profile Fragment", task.toString());
                }
            });
    }

    private void setLogOutButton() {
        logOutButton.setOnClickListener(view -> {
            showLogoutDialog();
        });
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.logout_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);

        MaterialButton btnCancel = dialog.findViewById(R.id.button_cancel);
        MaterialButton btnOk = dialog.findViewById(R.id.button_ok);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        });

        dialog.show();
    }
}