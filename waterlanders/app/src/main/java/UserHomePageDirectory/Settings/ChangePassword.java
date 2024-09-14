package UserHomePageDirectory.Settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.waterlanders.R;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import ForgotPasswordDirectory.ForgotPasswordPhoneNewPassword;
import ForgotPasswordDirectory.ForgotPasswordSuccess;
import Handler.PassUtils;

import java.util.regex.Pattern;

public class ChangePassword extends AppCompatActivity {

    private TextInputEditText currentPassword, newPassword, confirmPassword;
    private TextInputLayout currentPasswordLayout, newPasswordLayout, confirmPasswordLayout;
    private CardView updatePasswordButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String userEmail;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initializeObjects();
        getIntentData();


        updatePasswordButton.setOnClickListener(view -> checkInputData());
    }

    private void initializeObjects() {
        currentPassword = findViewById(R.id.currentPasswordField);
        newPassword = findViewById(R.id.newPasswordField);
        confirmPassword = findViewById(R.id.confirmPasswordField);
        currentPasswordLayout = findViewById(R.id.currentPasswordLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
    }

    private void checkInputData() {
        String currentPass = currentPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        if (TextUtils.isEmpty(currentPass)) {
            currentPasswordLayout.setError("Please enter your current password.");
            return;
        }

        if (TextUtils.isEmpty(newPass)) {
            newPasswordLayout.setError("Please enter a new password.");
            return;
        }

        if (newPass.length() < 8 || !hasNumberAndSymbol(newPass)) {
            newPasswordLayout.setError("Password must be at least 8 characters, and contain a number and a special symbol.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            confirmPasswordLayout.setError("Passwords do not match.");
            return;
        }

        reauthenticateAndChangePassword(currentPass, newPass);
    }

    private boolean hasNumberAndSymbol(String password) {
        Pattern numberPattern = Pattern.compile("[0-9]");
        Pattern symbolPattern = Pattern.compile("[^a-zA-Z0-9]");
        return numberPattern.matcher(password).find() && symbolPattern.matcher(password).find();
    }

    private void reauthenticateAndChangePassword(String currentPassword, String newPassword) {
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Proceed with updating the password
                    updatePassword(newPassword);
                } else {
                    Toast.makeText(ChangePassword.this, "Current password is incorrect.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getIntentData(){
        Intent intent = getIntent();
        userEmail = (String) intent.getSerializableExtra("user_email");
        userPassword = (String) intent.getSerializableExtra("user_pass");
    }

    private void updatePassword(String newPassword) {
        // sign in the user base on the previous user credentials
        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String hashedPassword = PassUtils.hashPassword(newPassword);
                            user.updatePassword(hashedPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // update the user pass in database
                                                String userId = user.getUid();
                                                db.collection("users").document(userId)
                                                        .update("password", hashedPassword)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Intent backIntent = new Intent(ChangePassword.this, ForgotPasswordSuccess.class);
                                                            backIntent.putExtra("success_message","Password Reset Successfully");
                                                            backIntent.putExtra("success_description","You successfully updated your password");
                                                            startActivity(backIntent);
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(ChangePassword.this, "ERROR CREATING AN ACCOUNT.\n" + e,
                                                                Toast.LENGTH_SHORT).show());
                                            } else {
                                                Toast.makeText(ChangePassword.this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(ChangePassword.this, "USER NOT FOUND IN DATABASE.", Toast.LENGTH_SHORT).show();
                        }

                    }else {
                        Log.d("FORGOT PASSWORD PHONE NEW PASSWORD", "Failed to re-authenticate user: " + task.getException().getMessage());
                        Toast.makeText(ChangePassword.this, "Failed to re-authenticate user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
