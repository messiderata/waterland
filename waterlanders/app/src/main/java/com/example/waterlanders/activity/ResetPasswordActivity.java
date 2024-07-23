package com.example.waterlanders.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextNewPassword;
    private Button buttonResetPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        mAuth = FirebaseAuth.getInstance();

        buttonResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        if (newPassword.isEmpty() || newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters");
            editTextNewPassword.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "Password reset successful", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Password reset failed", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
