package UserHomePageDirectory.Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.waterlanders.R;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ForgotPasswordDirectory.ForgotPasswordPhoneNewPassword;
import ForgotPasswordDirectory.ForgotPasswordSuccess;
import Handler.PassUtils;
import Handler.ShowToast;
import Handler.StatusBarUtil;

import java.util.regex.Pattern;

public class ChangePassword extends AppCompatActivity {

    private TextInputEditText currentPassword, newPassword, confirmPassword;
    private TextInputLayout currentPasswordLayout, newPasswordLayout, confirmPasswordLayout;
    private CardView updatePasswordButton;
    private ImageView backButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);

        initializeObjects();

        updatePasswordButton.setOnClickListener(view -> checkInputData());
        backButton.setOnClickListener(view -> finish());
    }

    private void initializeObjects() {
        currentPassword = findViewById(R.id.currentPasswordField);
        newPassword = findViewById(R.id.newPasswordField);
        confirmPassword = findViewById(R.id.confirmPasswordField);
        currentPasswordLayout = findViewById(R.id.currentPasswordLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        backButton = findViewById(R.id.btn_back);

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

        // this will ensure that the 'currentPass' string is converted
        // base on our encryption then start re-authenticate
        compareHashPasswords(currentPass, newPass);
    }

    private boolean hasNumberAndSymbol(String password) {
        Pattern numberPattern = Pattern.compile("[0-9]");
        Pattern symbolPattern = Pattern.compile("[^a-zA-Z0-9]");
        return numberPattern.matcher(password).find() && symbolPattern.matcher(password).find();
    }

    private void compareHashPasswords(String currentPassword, String newPass){
        if (user != null){
            db.collection("users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot userDoc = task.getResult();

                    if (userDoc.exists()){
                        String savePassword = userDoc.getString("password");
                        boolean isPasswordCorrect = PassUtils.checkPassword(currentPassword, savePassword);

                        if (isPasswordCorrect){
                            reauthenticateAndChangePassword(savePassword, newPass);
                        } else {
                            Toast.makeText(ChangePassword.this, "Current password is incorrect.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ChangePassword.this, "Error retrieving user data.", Toast.LENGTH_SHORT).show();
                    }
                });
        }
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

    private void updatePassword(String newPassword) {
        String hashedPassword = PassUtils.hashPassword(newPassword);
        user.updatePassword(hashedPassword)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    // Update 'password' field to new password
                    db.collection("users").document(user.getUid())
                            .update("password", hashedPassword)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ChangePassword.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.d("CHANGE PASSWORD", "Error updating password in database: " + e.getMessage());
                                Toast.makeText(ChangePassword.this, "Error updating password in database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                } else {
                    Log.d("FORGOT PASSWORD PHONE NEW PASSWORD", "Failed to re-authenticate user: " + task.getException().getMessage());
                    Toast.makeText(ChangePassword.this, "Failed to re-authenticate user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}
