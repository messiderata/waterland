package UserHomePageDirectory.AddressList;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Handler.StatusBarUtil;
import UserHomePageDirectory.AddedItems;
import UserHomePageDirectory.DeliveryDetails;
import UserHomePageDirectory.OrderConfirmation;

public class AddressInput extends AppCompatActivity {
    private static final String ITEM_ADDRESS = "NCR, Metro Manila, Marikina, Barangka";
    private static final String ITEM_POSTAL = "1803";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_input);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);


        AutoCompleteTextView fixAddressSelected = findViewById(R.id.fix_address_input);
        AutoCompleteTextView fixPostalSelected = findViewById(R.id.postal_input);
        ImageView backButton = findViewById(R.id.btn_back);
        CardView saveButton = findViewById(R.id.save_button);

        setupAutoCompleteTextView(fixAddressSelected, ITEM_ADDRESS);
        setupAutoCompleteTextView(fixPostalSelected, ITEM_POSTAL);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        backButton.setOnClickListener(view -> finish()); // Close AddressInput and return to AddressSelection

        saveButton.setOnClickListener(view -> validateInput());

        setupTextWatchers();
    }

    private void setupAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView, String item) {
        // Create a single-item adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{item});

        // Apply the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Optionally, set an item click listener
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = adapter.getItem(position);
            // Handle the selection if needed
        });
    }

    private void setupTextWatchers() {
        TextInputEditText phoneNumberInput = findViewById(R.id.phone_number_input);

        // Set input filter to limit length to 11
        phoneNumberInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    private void validateInput() {
        // Retrieve input text values
        String fullName = Objects.requireNonNull(((TextInputEditText) findViewById(R.id.full_name_input)).getText()).toString();
        String phoneNumber = Objects.requireNonNull(((TextInputEditText) findViewById(R.id.phone_number_input)).getText()).toString();
        String fixStreetInput = Objects.requireNonNull(((TextInputEditText) findViewById(R.id.street_input)).getText()).toString();
        String fixAddressInput = Objects.requireNonNull(((AutoCompleteTextView) findViewById(R.id.fix_address_input)).getText()).toString();
        String fixPostalCodeInput = Objects.requireNonNull(((AutoCompleteTextView) findViewById(R.id.postal_input)).getText()).toString();

        // Retrieve TextInputLayout references
        TextInputLayout fullNameInputLayout = findViewById(R.id.full_name_input_layout);
        TextInputLayout phoneNumberLayout = findViewById(R.id.phone_number_layout);
        TextInputLayout streetInputLayout = findViewById(R.id.street_input_layout);
        TextInputLayout addressInputLayout = findViewById(R.id.fix_address_input_layout);
        TextInputLayout postalCodeInputLayout = findViewById(R.id.postal_input_layout);

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
            phoneNumberLayout.setHelperText(null); // Clear any previous helper text
            phoneNumberLayout.setError("Phone number is required"); // Show error
            isValid = false;
        } else if (!phoneNumber.matches("^\\d{10}$")) {
            phoneNumberLayout.setHelperText(null); // Clear any previous helper text
            phoneNumberLayout.setError("Phone number must be exactly 10 digits. +63 is already given."); // Show error
            isValid = false;
        } else {
            phoneNumberLayout.setError(null); // Clear the error
        }

        // Validate Street Input
        if (TextUtils.isEmpty(fixStreetInput)) {
            streetInputLayout.setHelperText(null); // Clear any previous helper text
            streetInputLayout.setError("Street address is required"); // Show error
            isValid = false;
        } else {
            streetInputLayout.setError(null); // Clear the error
        }

        // Validate Address Input
         if (!isValidAddress(fixAddressInput)) {
            addressInputLayout.setHelperText(null); // Clear any previous helper text
            addressInputLayout.setError("Address is required"); // Show error
            isValid = false;
        }else {
            addressInputLayout.setError(null); // Clear the error
        }

        // Validate Postal Code
          if (!fixPostalCodeInput.matches("\\d{4}")) {
            postalCodeInputLayout.setHelperText(null); // Clear any previous helper text
            postalCodeInputLayout.setError("Postal code is required\""); // Show error
            isValid = false;
        } else {
            postalCodeInputLayout.setError(null); // Clear the error
        }

        // Show Toast if any errors
        if (!isValid) {
            Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show();
        } else {
            String fullAddress = fixStreetInput + ", " + fixAddressInput + ", " + fixPostalCodeInput;
            String formattedPhoneNumber = String.format("+63"+phoneNumber);

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
}
