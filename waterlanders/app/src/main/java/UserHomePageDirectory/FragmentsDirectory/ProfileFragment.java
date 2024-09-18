package UserHomePageDirectory.FragmentsDirectory;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

import LoginDirectory.Login;


public class ProfileFragment extends Fragment {

    TextView username;
    CardView logOutButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize your CardView buttons here
        username = view.findViewById(R.id.user_username);
        logOutButton = view.findViewById(R.id.logout_button_settings);

        setLogOutButton();


        // Retrieve and display user data
        FireStoreHelper firestoreHelper = new FireStoreHelper();
        firestoreHelper.getUserData(new FireStoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                displayCurrentUser(document);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle error case here, like showing a Toast or a Snackbar
                // e.g., Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });



        return view;
    }

    private void displayCurrentUser(DocumentSnapshot document) {
        String userUserName = document.getString("username");

        username.setText(userUserName);
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

    private void setLogOutButton() {
        logOutButton.setOnClickListener(view -> {
            showLogoutDialog();
        });
    }
}