package UserHomePageDirectory.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Handler.StatusBarUtil;

public class EditProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> contactNumbers;

    private TextInputEditText nameInput, nicknameInput, phoneInput;
    private CardView saveButton;
    private ImageView backButton;

    // Fields to hold current user data
    private String currentFullName, currentUsername, currentPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        contactNumbers = new ArrayList<>();
        initializeCurrentUserData();

        // Initialize UI components
        nameInput = findViewById(R.id.name_input);
        nicknameInput = findViewById(R.id.nickname_input);
        phoneInput = findViewById(R.id.phone_input);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.btn_back);

        // Set input filter to limit length to 10
        phoneInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        // Set up the listener for the save button
        saveButton.setOnClickListener(view -> isContactNumberUnique(this::validateAndSaveUserProfile));
        backButton.setOnClickListener(view -> finish());
    }

    // Method to mask phone number (show first 2 and last 2 digits, mask the rest)
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() >= 4) {
            String firstTwo = phoneNumber.substring(0, 2); // First 2 digits
            String lastTwo = phoneNumber.substring(phoneNumber.length() - 2); // Last 2 digits
            String maskedPart = new String(new char[phoneNumber.length() - 4]).replace("\0", "*"); // Mask middle part
            return firstTwo + maskedPart + lastTwo;
        }
        return phoneNumber; // Return original if too short to mask
    }

    private void initializeCurrentUserData(){
        String userID = mAuth.getCurrentUser().getUid();
        db.collection("users")
            .document(userID)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null){
                    currentFullName = task.getResult().getString("fullName");
                    currentUsername = task.getResult().getString("username");

                    List<Map<String, Object>> deliveryDetails = (List<Map<String, Object>>) task.getResult().get("deliveryDetails");
                    if (deliveryDetails != null) {
                        for (Map<String, Object> details : deliveryDetails) {
                            // Convert 'isDefaultAddress' to int
                            int defaultAddress = details.get("isDefaultAddress") instanceof Long
                                    ? ((Long) details.get("isDefaultAddress")).intValue()
                                    : (Integer) details.get("isDefaultAddress");

                            if (defaultAddress == 1) {
                                currentPhone = (String) details.get("phoneNumber");
                                break;
                            }
                        }
                    }
                } else {
                    Log.e("Edit Profile", task.toString());
                }
            })
            .addOnFailureListener(task -> {
                Log.e("Edit Profile", task.toString());
            });
    }

    private void isContactNumberUnique(Runnable callback) {
        contactNumbers.clear();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> usersList = task.getResult().getDocuments();
                for (DocumentSnapshot user : usersList) {
                    List<Map<String, Object>> deliveryDetailsList = (List<Map<String, Object>>) user.get("deliveryDetails");
                    if (deliveryDetailsList != null) {
                        for (Map<String, Object> details : deliveryDetailsList) {
                            contactNumbers.add(String.valueOf(details.get("phoneNumber")));
                        }
                    }
                }
                callback.run(); // Proceed with validation after fetching all contact numbers
            } else {
                Toast.makeText(this, "Failed to retrieve users data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndSaveUserProfile() {
        String name = nameInput.getText().toString().trim();
        String username = nicknameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // validate phone number
        // we introduce new phone number validation
        // in new user registration for compatibility of
        // sending code in phone number
        // this way if client need to integrate into sms service
        // the integration will be easy since the phone number
        // is already formatted with country code
        if (!phone.isEmpty() && !phone.matches("^\\d{10}$")) {
            Toast.makeText(this, "Phone number must be exactly 10 digits. +63 is already given.", Toast.LENGTH_LONG).show();
            return;
        } else if (!phone.isEmpty() && contactNumbers.contains(String.format("+63" + phone))) {
            Toast.makeText(this, "Phone number already registered.", Toast.LENGTH_LONG).show();
            return;
        }

        // If username is entered, check for uniqueness
        if (!username.isEmpty() && !username.equals(currentUsername)) {
            checkIfUsernameIsUnique(username, name, phone);
        } else {
            // No need to check for username uniqueness if it's not provided or hasn't changed
            saveUserProfile(name, username, phone);
        }
    }

    private void checkIfUsernameIsUnique(String username, String name, String phone) {
        // Query Firestore to check if the username already exists (but not for the current user)
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!isUsernameUnique(querySnapshot)) {
                    Toast.makeText(EditProfile.this, "Username is already taken, please choose another", Toast.LENGTH_SHORT).show();
                } else {
                    // Username is unique, proceed to update the profile
                    saveUserProfile(name, username, phone);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(EditProfile.this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private boolean isUsernameUnique(QuerySnapshot querySnapshot) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        // If there are results, check that the user doesn't already have this username
        if (!querySnapshot.isEmpty()) {
            // Check if the only result is the current user's own document
            return querySnapshot.size() == 1 && querySnapshot.getDocuments().get(0).getId().equals(currentUserId);
        }
        return true; // No other user has this username
    }

    private void saveUserProfile(String name, String username, String phone) {
        // Get the user's UID
        String userId = mAuth.getCurrentUser().getUid();
        String formattedPhoneNumber = "+63" + phone;

        // Create a map of the updated user data
        Map<String, Object> userData = new HashMap<>();

        // Only add fields that are not empty and different from current values
        if (!name.isEmpty() && !name.equals(currentFullName)) {
            userData.put("fullName", name);
        }
        if (!username.isEmpty() && !username.equals(currentUsername)) {
            userData.put("username", username);
        }


        // If no fields are updated, show a message and don't proceed with update
        if (userData.isEmpty()) {
            if (!phone.isEmpty() && !formattedPhoneNumber.equals(currentPhone)) {
                savePhoneNumberAsPrimary(formattedPhoneNumber);
            } else {
                Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update Firestore document
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.update(userData)
                .addOnSuccessListener(aVoid -> {
                    if (!phone.isEmpty() && !formattedPhoneNumber.equals(currentPhone)) {
                        savePhoneNumberAsPrimary(formattedPhoneNumber);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    // since the user want to change their mobile number
    // we save it on their 'deliveryDetails' field default address
    // where in we change their last 'phoneNumber' to the new 'formattedPhoneNumber'
    private void savePhoneNumberAsPrimary(String formattedPhoneNumber){
        String userID = mAuth.getCurrentUser().getUid();
        db.collection("users")
            .document(userID)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null){
                    List<Map<String, Object>> deliveryDetails = (List<Map<String, Object>>) task.getResult().get("deliveryDetails");
                    if (deliveryDetails != null){
                        boolean isUpdated = false;

                        for (Map<String, Object> details : deliveryDetails) {
                            // Convert 'isDefaultAddress' to int
                            int defaultAddress = details.get("isDefaultAddress") instanceof Long
                                    ? ((Long) details.get("isDefaultAddress")).intValue()
                                    : (Integer) details.get("isDefaultAddress");

                            if (defaultAddress == 1) {
                                // Update the phone number
                                details.put("phoneNumber", formattedPhoneNumber);
                                isUpdated = true;
                            }
                        }

                        if (isUpdated) {
                            // Save the updated list back to Firestore
                            db.collection("users")
                                    .document(userID)
                                    .update("deliveryDetails", deliveryDetails)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        // Reload the activity to refresh the data
                                        reloadActivity();
                                    })
                                    .addOnFailureListener(e -> Log.e("Edit Profile", "Error updating phone number: " + e.getMessage()));
                        } else {
                            Log.d("Edit Profile", "No default address found");
                        }
                    }

                } else {
                    Log.e("Edit Profile", task.toString());
                }
            });
    }

    // Method to reload the current activity
    private void reloadActivity() {
        Intent intent = getIntent();
        finish(); // Finish the current activity
        startActivity(intent); // Start the same activity again to refresh the data
    }
}
