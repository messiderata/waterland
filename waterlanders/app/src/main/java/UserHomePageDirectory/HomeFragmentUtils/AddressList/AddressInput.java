package UserHomePageDirectory.HomeFragmentUtils.AddressList;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Handler.StatusBarUtil;
import UserHomePageDirectory.HomeFragmentUtils.AddedItems;

public class AddressInput extends AppCompatActivity {

    private ImageView backButton;

    private TextInputLayout fullNameInputLayout;
    private TextInputEditText fullNameInput;
    private TextInputLayout phoneNumberLayout;
    private TextInputEditText phoneNumberInput;
    private TextInputLayout fixAddressInputLayout;
    private AutoCompleteTextView fixAddressInput;
    private TextInputLayout postalInputLayout;
    private AutoCompleteTextView fixPostalInput;
    private TextInputLayout streetInputLayout;
    private TextInputEditText streetInput;

    private CardView saveButton;

    private static final String ITEM_ADDRESS = "NCR, Metro Manila, Marikina, Barangka";
    private static final String ITEM_POSTAL = "1803";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<String> contactNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_input);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);
        initializeObjects();
        setupAutoCompleteTextView(fixAddressInput, ITEM_ADDRESS);
        setupAutoCompleteTextView(fixPostalInput, ITEM_POSTAL);

        backButton.setOnClickListener(view -> finish());

        saveButton.setOnClickListener(view -> {
            // Check if phone number is unique before validating other inputs
            isContactNumberUnique(this::validateInput);
        });
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.btn_back);

        fullNameInputLayout = findViewById(R.id.full_name_input_layout);
        fullNameInput = findViewById(R.id.full_name_input);
        phoneNumberLayout = findViewById(R.id.phone_number_layout);
        phoneNumberInput = findViewById(R.id.phone_number_input);
        fixAddressInputLayout = findViewById(R.id.fix_address_input_layout);
        fixAddressInput = findViewById(R.id.fix_address_input);
        postalInputLayout = findViewById(R.id.postal_input_layout);
        fixPostalInput = findViewById(R.id.postal_input);
        streetInputLayout = findViewById(R.id.street_input_layout);
        streetInput = findViewById(R.id.street_input);

        saveButton = findViewById(R.id.save_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        contactNumbers = new ArrayList<>();

        // Set input filter to limit length to 10
        phoneNumberInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    private void setupAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView, String item) {
        // Create a single-item adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{item});

        // Apply the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);
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

    private void validateInput() {
        // Retrieve input text values
        String fullName = String.valueOf(fullNameInput.getText());
        String phoneNumber = String.valueOf(phoneNumberInput.getText());
        String fixAddress = String.valueOf(fixAddressInput.getText());
        String fixPostalCode = String.valueOf(fixPostalInput.getText());
        String customStreet = String.valueOf(streetInput.getText());

        boolean isValid = true;

        // Validate Full Name
        if (TextUtils.isEmpty(fullName)) {
            fullNameInputLayout.setHelperText(null); // Clear any previous helper text
            fullNameInputLayout.setError("Full name is required"); // Show error
            isValid = false;
        } else {
            fullNameInputLayout.setError(null); // Clear the error
        }

        // Validate Phone Number
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberLayout.setHelperText(null);
            phoneNumberLayout.setError("Phone number is required");
            isValid = false;
        } else if (!phoneNumber.matches("^\\d{10}$")) {
            phoneNumberLayout.setHelperText(null);
            phoneNumberLayout.setError("Phone number must be exactly 10 digits. +63 is already given.");
            isValid = false;
        } else if (contactNumbers.contains(String.format("+63" + phoneNumber))) {
            Toast.makeText(this, "Phone number already registered.", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else {
            phoneNumberLayout.setError(null);
        }

        // Validate Address Input
        if (!isValidAddress(fixAddress)) {
             fixAddressInputLayout.setHelperText(null);
             fixAddressInputLayout.setError("Address is required");
             isValid = false;
        }else {
            fixAddressInputLayout.setError(null);
        }

        // Validate Postal Code
        if (!fixPostalCode.matches("\\d{4}")) {
            postalInputLayout.setHelperText(null);
            postalInputLayout.setError("Postal code is required\"");
            isValid = false;
        } else {
            postalInputLayout.setError(null);
        }

        // Validate Street Input
        if (TextUtils.isEmpty(customStreet)) {
            streetInputLayout.setHelperText(null);
            streetInputLayout.setError("Street address is required");
            isValid = false;
        } else {
            streetInputLayout.setError(null);
        }

        // Show Toast if any errors
        if (!isValid) {
            Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show();
        } else {
            String fullAddress = customStreet + ", " + fixAddress + ", " + fixPostalCode;
            String formattedPhoneNumber = String.format("+63"+phoneNumber);
            saveCurrentUserDefaultAddress(fullName, formattedPhoneNumber, fullAddress);
        }
    }

    private boolean isValidAddress(String address) {
        // Here you should check if the address matches your list of valid options
        // You can use a predefined list or database to verify the address
        String[] validAddresses = {"NCR, Metro Manila, Marikina, Barangka"}; // Example list
        for (String validAddress : validAddresses) {
            if (address.equalsIgnoreCase(validAddress)) {
                return true;
            }
        }
        return false;
    }

    // flow
    // it will first check the current user's default address
    // then it will change the default address to the newly added one
    private void saveCurrentUserDefaultAddress(String fullName, String formattedPhoneNumber, String fullAddress){
        // Get the current user data from the database
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> deliveryDetailsList = (List<Map<String, Object>>) documentSnapshot.get("deliveryDetails");

                        if (deliveryDetailsList != null) {
                            boolean foundDefault = false;

                            // this block of code will iterate to the address added by the user
                            // then turn the default address to 0
                            // to set later the newly added address to 1
                            for (Map<String, Object> details : deliveryDetailsList) {
                                if ((long) details.get("isDefaultAddress") == 1) {
                                    details.put("isDefaultAddress", 0);
                                    foundDefault = true;
                                    Log.d("AddressInput", "default address value: "+ details.get("isDefaultAddress"));
                                    break;
                                }
                            }

                            // Add the new address with isDefaultAddress set to 1
                            Map<String, Object> newDetail = new DeliveryDetails(fullName, formattedPhoneNumber, fullAddress, 1).toMap();

                            if (foundDefault) {
                                deliveryDetailsList.add(newDetail);
                                db.collection("users").document(userId)
                                    .update("deliveryDetails", deliveryDetailsList)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("AddressInput", "New address added successfully.");
                                        Toast.makeText(this, "New address added successfully.", Toast.LENGTH_SHORT).show();

                                        Intent intentAddressInput = new Intent(this, AddressSelection.class);
                                        intentAddressInput.putExtra("addedItems", (AddedItems) getIntent().getSerializableExtra("addedItems"));
                                        startActivity(intentAddressInput);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("AddressInput", "Error: ", e);
                                        Toast.makeText(this, "Error updating address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            } else {
                                Log.d("AddressInput", "No default address found to update.");
                                Toast.makeText(this, "No default address found to update.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("AddressInput", "No delivery details found.");
                            Toast.makeText(this, "No delivery details found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("AddressInput", "User data does not exist");
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AddressInput", "Error fetching user data", e);
                    Toast.makeText(this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                });
        } else {
            Log.d("AddressInput", "User not authenticated");
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }
}
