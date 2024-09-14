package UserHomePageDirectory.Settings;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

        // Initialize UI components
        nameInput = findViewById(R.id.name_input);
        nicknameInput = findViewById(R.id.nickname_input);
        phoneInput = findViewById(R.id.phone_input);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.btn_back);


        // Set up the listener for the save button
        saveButton.setOnClickListener(view -> validateAndSaveUserProfile());
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

    private void validateAndSaveUserProfile() {
        String name = nameInput.getText().toString().trim();
        String username = nicknameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Validate phone number (if entered, should start with "09" and be exactly 11 digits)
        if (!phone.isEmpty() && (!phone.startsWith("09") || phone.length() != 11)) {
            Toast.makeText(this, "Phone number must start with '09' and be 11 digits long", Toast.LENGTH_SHORT).show();
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

        // Create a map of the updated user data
        Map<String, Object> userData = new HashMap<>();

        // Only add fields that are not empty and different from current values
        if (!name.isEmpty() && !name.equals(currentFullName)) {
            userData.put("fullName", name);
        }
        if (!username.isEmpty() && !username.equals(currentUsername)) {
            userData.put("username", username);
        }
        if (!phone.isEmpty() && !phone.equals(currentPhone)) {
            userData.put("phone", phone);
        }

        // If no fields are updated, show a message and don't proceed with update
        if (userData.isEmpty()) {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore document
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    // Reload the activity to refresh the data
                    reloadActivity();
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Method to reload the current activity
    private void reloadActivity() {
        Intent intent = getIntent();
        finish(); // Finish the current activity
        startActivity(intent); // Start the same activity again to refresh the data
    }
}
