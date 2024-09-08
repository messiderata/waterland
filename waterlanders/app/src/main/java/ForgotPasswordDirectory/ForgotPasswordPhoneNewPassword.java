package ForgotPasswordDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import SignUpDirectory.UserSignUpSuccess;


public class ForgotPasswordPhoneNewPassword extends AppCompatActivity {

    private EditText newP;
    private EditText conP;
    private Button confirm;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_phone_new_password);
        initializeObjects();

        confirm.setOnClickListener(view -> checkInputData());
    }

    private void initializeObjects(){
        newP = findViewById(R.id.newP);
        conP = findViewById(R.id.conP);
        confirm = findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
    }

    private void checkInputData(){
        boolean isValid = true;
        if (TextUtils.isEmpty(newP.getText())){
            isValid = false;
            Toast.makeText(this, "Input your Password first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(conP.getText())){
            isValid = false;
            Toast.makeText(this, "Confirm your Password first.", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(newP.getText()).length() < 8) {
            isValid = false;
            Toast.makeText(this, "Password must be 8 characters and above.", Toast.LENGTH_SHORT).show();
        } else if (!String.valueOf(newP.getText()).equals(String.valueOf(conP.getText()))){
            isValid = false;
            Toast.makeText(this, "Password not match.", Toast.LENGTH_SHORT).show();
        }

        if (isValid){
            updatePassword(String.valueOf(newP.getText()));
        }
    }

    private void updatePassword(String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser(); // Get the currently authenticated user

        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent backIntent = new Intent(ForgotPasswordPhoneNewPassword.this, ForgotPasswordSuccess.class);
                        backIntent.putExtra("success_message","Password Reset Successfully");
                        backIntent.putExtra("success_description","You successfully updated your password");
                        startActivity(backIntent);
                    } else {
                        Toast.makeText(ForgotPasswordPhoneNewPassword.this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            Toast.makeText(this, "User is not authenticated. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}