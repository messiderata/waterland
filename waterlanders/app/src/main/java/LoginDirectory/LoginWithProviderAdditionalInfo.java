package LoginDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SignUpDirectory.UserSignUpSuccess;

public class LoginWithProviderAdditionalInfo extends AppCompatActivity {

    private TextInputEditText fullName;
    private TextInputEditText userName;
    private AutoCompleteTextView addressInput;
    private AutoCompleteTextView postalInput;
    private TextInputEditText streetAddress;
    private TextInputEditText contactNumber;
    private CardView registerButton;

    private String userEmail;
    private String userFullName;
    private String hashedPassword;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final String ITEM_ADDRESS = "NCR, Metro Manila, Marikina, Barangka";
    private static final String ITEM_POSTAL = "1803";

    // whole flow
    // since we already successfully sign in the user base on their
    // facebook credentials or google credentials
    // we ask for additional information that our app need
    // such as address, contact number, and their own unique username
    // these details are too sensitive to share with us by other parties
    // so we ask the user manually
    // the username and contact number must be unique, why?
    // username can be use to login to our app so we want every user to
    // have a unique username
    // contact number is used in delivery details
    // for our courier or admin to contact the customer
    // if we have duplicate numbers we will have a huge problem
    // if the phone number is not valid, admin and courier can just ignore the order
    // and let the devs remove manually the order for safety purposes
    // we cant just give access even to the admin to delete orders
    // as well as to the couriers because it can lead to abuse

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_with_provider_additional_info);
        initializeObject();
        getIntentData();
        setFixAddress();

        registerButton.setOnClickListener(view -> {
            checkInputDetails();
        });
    }

    private void initializeObject(){
        fullName = findViewById(R.id.full_name);
        userName = findViewById(R.id.user_name);
        addressInput = findViewById(R.id.address_input);
        postalInput = findViewById(R.id.postal_input);
        streetAddress = findViewById(R.id.street_address);
        contactNumber = findViewById(R.id.contact_number);
        registerButton = findViewById(R.id.register_button);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // set filter for contact number
        contactNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    private void getIntentData(){
        Intent intent = getIntent();
        userEmail = (String) intent.getSerializableExtra("user_email");
        userFullName = (String) intent.getSerializableExtra("user_fullName");
        hashedPassword = (String) intent.getSerializableExtra("hashedPassword");

        // initially set the text of fullName and username of the user base on the retrieve
        // data from the login provider
        // customer can edit this later on
        fullName.setText(userFullName);
        userName.setText(userFullName);
    }

    private void setFixAddress(){
        ArrayAdapter<String> addressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{ITEM_ADDRESS});
        addressInput.setAdapter(addressAdapter);

        ArrayAdapter<String> postalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{ITEM_POSTAL});
        postalInput.setAdapter(postalAdapter);
    }

    private void checkInputDetails(){
        boolean isComplete = true;
        if (TextUtils.isEmpty(fullName.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Full Name first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(userName.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your User Name first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(addressInput.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Address first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(postalInput.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Postal first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(streetAddress.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Street first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(contactNumber.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Contact Number first.", Toast.LENGTH_SHORT).show();
        } else if (!String.valueOf(contactNumber.getText()).matches("^\\d{10}$")) {
            isComplete = false;
            Toast.makeText(this, "Phone number must be exactly 10 digits. +63 is already given.", Toast.LENGTH_SHORT).show();
        }

        if (isComplete){
            isUsernameUnique(String.valueOf(userName.getText()));
        }
    }

    private void isUsernameUnique(String username){
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // Username already exists
                    Toast.makeText(this, "Username already registered.", Toast.LENGTH_SHORT).show();

                } else {
                    // Email and Username are unique, proceed to create user
                    createUser();
                }
            });
    }

    private void createUser(){
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("email", userEmail);
        newUser.put("fullName", String.valueOf(fullName.getText()));
        newUser.put("username", String.valueOf(userName.getText()));
        newUser.put("role", "customer");
        newUser.put("isResetPassTruEmail", 0);

        List<Map<String, Object>> deliveryDetailsList = new ArrayList<>();
        Map<String, Object> deliveryDetails = new HashMap<>();
        String formatedAddress = String.format(streetAddress.getText() + ", " + addressInput.getText() + ", " + postalInput.getText());
        deliveryDetails.put("deliveryAddress", formatedAddress);
        deliveryDetails.put("fullName", String.valueOf(fullName.getText()));
        deliveryDetails.put("isDefaultAddress", 1);

        String formattedPhoneNumber = String.format("+63"+contactNumber.getText());
        deliveryDetails.put("phoneNumber", formattedPhoneNumber);
        deliveryDetailsList.add(deliveryDetails);

        newUser.put("deliveryDetails", deliveryDetailsList);
        newUser.put("password", hashedPassword);

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
            .set(newUser)
            .addOnSuccessListener(aVoid -> {
                Intent successIntent = new Intent(this, UserSignUpSuccess.class);
                successIntent.putExtra("success_message","Account Registered");
                successIntent.putExtra("success_description","The account is successfully registered");
                startActivity(successIntent);
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "ERROR CREATING AN ACCOUNT.\n" + e,
                    Toast.LENGTH_SHORT).show());
    }
}