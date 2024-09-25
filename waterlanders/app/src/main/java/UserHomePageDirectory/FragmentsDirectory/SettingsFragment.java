package UserHomePageDirectory.FragmentsDirectory;

import static android.content.Intent.getIntent;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waterlanders.R;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import LoginDirectory.Login;
import UserHomePageDirectory.HomeFragmentUtils.AddedItems;
import UserHomePageDirectory.HomeFragmentUtils.AddressList.AddressInput;
import UserHomePageDirectory.HomeFragmentUtils.AddressList.AddressSelection;
import UserHomePageDirectory.MainDashboardUser;
import UserHomePageDirectory.Settings.ChangePassword;
import UserHomePageDirectory.Settings.EditProfile;

public class SettingsFragment extends Fragment {

    CardView profileBtn, logOutButton, deleteAccountButton, changePassButton, addressButton;
    TextView nameText, userNameText, phoneText, emailText;
    public static final int REQUEST_CODE_ADDRESS_SELECTION = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        // Initialize buttons and text views
        initUI(view);
        // Set up listeners for buttons
        setupProfileButton();
        ChangePass();
        setDeleteAccountButton();  // Ensure this method is called to set up the delete account button
        addressScreen();

        // Fetch and display user data from Firestore
        fetchUserData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload the user data when returning to this fragment
        fetchUserData();
    }

    private void initUI(View view) {
        profileBtn = view.findViewById(R.id.My_profile_button);
        deleteAccountButton = view.findViewById(R.id.delete_button_settings);  // Ensure this ID is correct

        nameText = view.findViewById(R.id.name_text);
        userNameText = view.findViewById(R.id.username_text);
        phoneText = view.findViewById(R.id.phone_text);
        emailText = view.findViewById(R.id.email_text);
        changePassButton = view.findViewById(R.id.change_pass_button);
        addressButton = view.findViewById(R.id.address_button);
    }

    private void setupProfileButton() {
        profileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), EditProfile.class);
            startActivity(intent);
        });
    }


    private void ChangePass() {
        changePassButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ChangePassword.class);
            startActivity(intent);
        });
    }


    private void addressScreen() {
        addressButton.setOnClickListener(view -> {
            AddedItems addedItems = new AddedItems();
            Intent intent = new Intent(getActivity(), AddressSelection.class);
            intent.putExtra("addedItems", addedItems);
            intent.putExtra("intentFrom", "SettingsFragment");
            startActivity(intent);
        });
    }



    private void setDeleteAccountButton() {
        deleteAccountButton.setOnClickListener(view -> {
            showDeleteAccountDialog();
        });
    }



    private void fetchUserData() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userDoc = firestore.collection("users").document(userId);

        userDoc.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                displayCurrentUser(document);
            } else {
                Toast.makeText(getContext(), "Document is null", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_LONG).show();
        });
    }

    private void displayCurrentUser(DocumentSnapshot document) {
        String email = document.getString("email");
        String fullName = document.getString("fullName");
        String username = document.getString("username");
        String phoneNumber = "";

        List<Map<String, Object>> deliveryDetails = (List<Map<String, Object>>) document.get("deliveryDetails");
        if (deliveryDetails != null) {
            for (Map<String, Object> details : deliveryDetails) {
                // Convert 'isDefaultAddress' to int
                int defaultAddress = details.get("isDefaultAddress") instanceof Long
                        ? ((Long) details.get("isDefaultAddress")).intValue()
                        : (Integer) details.get("isDefaultAddress");

                if (defaultAddress == 1) {
                    phoneNumber = (String) details.get("phoneNumber");
                    break;
                }
            }
        }

        nameText.setText(fullName != null ? fullName : "N/A");
        emailText.setText(maskEmail(email));
        userNameText.setText(username != null ? username : "N/A");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneText.setText("Add mobile number");
        } else {
            phoneText.setText(maskPhoneNumber(phoneNumber));
        }
    }

    private String maskEmail(String email) {
        if (email != null && email.contains("@")) {
            int atIndex = email.indexOf("@");
            String firstLetter = email.substring(0, 1);
            String maskedPart = new String(new char[atIndex - 1]).replace('\0', '*');
            return firstLetter + maskedPart + email.substring(atIndex);
        }
        return "N/A";
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.length() > 4) {
            String firstTwo = phoneNumber.substring(0, 5);
            String lastTwo = phoneNumber.substring(phoneNumber.length() - 2);
            String middlePart = phoneNumber.substring(5, phoneNumber.length() - 2).replaceAll(".", "*");
            return firstTwo + middlePart + lastTwo;
        }
        return "N/A";
    }

    private void showDeleteAccountDialog() {
        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.deleteaccount_dialog);  // Make sure to create this layout file
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);

        MaterialButton btnCancel = dialog.findViewById(R.id.button_cancel);
        MaterialButton btnDelete = dialog.findViewById(R.id.button_delete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            deleteAccount(firestore);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteAccount(FirebaseFirestore firestore) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        DocumentReference userDoc = firestore.collection("users").document(userId);
        userDoc.delete().addOnSuccessListener(aVoid -> {
            auth.getCurrentUser().delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireActivity(), Login.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
        });
    }
}
