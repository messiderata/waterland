package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class update_password extends AppCompatActivity {

    TextInputEditText edit_new_pass, edit_conf_pass;
    Button btn_submit;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edit_new_pass = findViewById(R.id.new_pass);
        edit_conf_pass = findViewById(R.id.confirm_pass);
        btn_submit = findViewById(R.id.submit_btn);
        progressBar = findViewById(R.id.progress_bar);

        btn_submit.setOnClickListener(view -> {
            String newPass = String.valueOf(edit_new_pass.getText());
            String confPass = String.valueOf(edit_conf_pass.getText());

            if (TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confPass)) {
                Toast.makeText(update_password.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confPass)) {
                Toast.makeText(update_password.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            updatePassword(newPass);
        });
    }

    private void updatePassword(String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("hellow123123", "updatePassword user: "+ Objects.requireNonNull(user).getEmail());
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(update_password.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            // Redirect to login screen or another activity
                            Intent intent = new Intent(update_password.this, update_pass_success.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(update_password.this, "Failed to update password: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(update_password.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}