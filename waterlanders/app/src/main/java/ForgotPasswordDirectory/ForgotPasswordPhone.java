package ForgotPasswordDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import SignUpDirectory.UserSignUpAdditionalInfo;


public class ForgotPasswordPhone extends AppCompatActivity {

    private ImageView backIcon;
    private EditText phoneField;
    private Button sendButton;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password_phone);
        initializeObject();

        sendButton.setOnClickListener(view -> checkInputDetails());

        backIcon.setOnClickListener(view -> finish());
    }

    private void initializeObject(){
        backIcon = findViewById(R.id.backIcon);
        phoneField = findViewById(R.id.phoneField);
        sendButton = findViewById(R.id.sendButton);

        db = FirebaseFirestore.getInstance();

        // set filter for contact number
        phoneField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    private void checkInputDetails(){
        boolean isComplete = true;
        if (!String.valueOf(phoneField.getText()).matches("^\\d{10}$")) {
            isComplete = false;
            Toast.makeText(this, "Phone number must be exactly 10 digits. +63 is already given.", Toast.LENGTH_SHORT).show();
        }

        if (isComplete){
            isPhoneNumberExist(String.format("+63"+phoneField.getText()));
        }
    }

    // flow
    // 1. retrieve the 'users' collection
    // 2. for every user in 'users' we will check only the account that has 'customer' role
    //      to prevent manipulating other accounts such as ADMIN and DELIVERY
    //      because that should be handle by the devs only
    // 3. we will check for the user's 'deliveryDetails' field to check which
    //      default address where the primary contact number is
    // 4. if the primary contact number is the same as user's inputed mobile number
    //      then send the otp to the mobile number
    //      else display 'Phone number is not registered'
    private void isPhoneNumberExist(String phoneNumber){
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> usersList = task.getResult().getDocuments();
                boolean phoneNumberFound = false;

                for (DocumentSnapshot user : usersList) {
                    String userRole = (String) user.get("role");

                    if (userRole != null && userRole.equals("customer")) {
                        List<Map<String, Object>> deliveryDetailsList = (List<Map<String, Object>>) user.get("deliveryDetails");

                        for (Map<String, Object> details : deliveryDetailsList) {
                            // Check if 'isDefaultAddress' is set to 1
                            Object isDefaultAddressObj = details.get("isDefaultAddress");

                            if (isDefaultAddressObj instanceof Long) {
                                Long isDefaultAddressLong = (Long) isDefaultAddressObj;
                                if (isDefaultAddressLong == 1L) {
                                    // If it is 1, retrieve the 'phoneNumber'
                                    String retrievedPhoneNumber = (String) details.get("phoneNumber");
                                    if (retrievedPhoneNumber != null && retrievedPhoneNumber.equals(phoneNumber)) {
                                        // Phone number exists
                                        String userEmail = (String) user.get("email");
                                        String userPassword = (String) user.get("password");
                                        Intent verifyOTPIntent = new Intent(ForgotPasswordPhone.this, ForgotPasswordPhoneOTP.class);
                                        verifyOTPIntent.putExtra("phone_number", phoneNumber);
                                        verifyOTPIntent.putExtra("user_email", userEmail);
                                        verifyOTPIntent.putExtra("user_pass", userPassword);
                                        startActivity(verifyOTPIntent);
                                        phoneNumberFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (phoneNumberFound) {
                        break;
                    }
                }

                if (!phoneNumberFound) {
                    // Phone number is not registered
                    Toast.makeText(this, "Phone number is not registered.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to retrieve users data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}