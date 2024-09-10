package SignUpDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.waterlanders.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserSignUp extends AppCompatActivity {

    private EditText emailField;
    private EditText passField;
    private EditText confirmField;
    private CardView registerButton;
    private TextView login_account;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);
        initializeObjects();

        // on click listeners fot the buttons
        registerButton.setOnClickListener(view -> checkInputData());
        login_account.setOnClickListener(view -> finish());
    }

    private void initializeObjects(){
        emailField = findViewById(R.id.emailField);
        passField = findViewById(R.id.passField);
        confirmField = findViewById(R.id.confirmField);
        registerButton = findViewById(R.id.registerButton);
        login_account = findViewById(R.id.login_account);

        db = FirebaseFirestore.getInstance();
    }

    private void checkInputData(){
        // this method will check each field
        // has valid values
        boolean isValid = true;
        if (TextUtils.isEmpty(emailField.getText())){
            isValid = false;
            Toast.makeText(this, "Input your Email first.", Toast.LENGTH_SHORT).show();
        } else if (!isEmailValid(String.valueOf(emailField.getText()))) {
            isValid = false;
            Toast.makeText(this, "Invalid Email format.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(passField.getText())){
            isValid = false;
            Toast.makeText(this, "Input your Password first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmField.getText())){
            isValid = false;
            Toast.makeText(this, "Confirm your Password first.", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(passField.getText()).length() < 8) {
            isValid = false;
            Toast.makeText(this, "Password must be 8 characters and above.", Toast.LENGTH_SHORT).show();
        } else if (!String.valueOf(passField.getText()).equals(String.valueOf(confirmField.getText()))){
            isValid = false;
            Toast.makeText(this, "Password not match.", Toast.LENGTH_SHORT).show();
        }

        // check if user want to sign up with email or contact number


        if (isValid){
            isEmailUnique(String.valueOf(emailField.getText()));
        }
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void isEmailUnique(String email) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // Email already exists
                    Toast.makeText(this, "Email already registered.", Toast.LENGTH_SHORT).show();
                } else {
                    // Email is unique
                    Intent showAdditionalInformationIntent = new Intent(this, UserSignUpAdditionalInfo.class);
                    showAdditionalInformationIntent.putExtra("user_email", String.valueOf(emailField.getText()));
                    showAdditionalInformationIntent.putExtra("user_pass", String.valueOf(passField.getText()));
                    startActivity(showAdditionalInformationIntent);
                }
            });
    }
}