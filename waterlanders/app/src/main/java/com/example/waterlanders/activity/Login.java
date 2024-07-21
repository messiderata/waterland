package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class Login extends AppCompatActivity {

    TextInputEditText editLoginAcc, editLoginPass;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView txtForgotPass, txtCreateAcc, loginText;

    int timeDelayInMillis = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize objects
        editLoginAcc = findViewById(R.id.login_account);
        editLoginPass = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.progress_bar);
        CardView btn_login = findViewById(R.id.login_button);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        txtForgotPass = findViewById(R.id.forgot_password);
        txtCreateAcc = findViewById(R.id.create_account);
        loginText = findViewById(R.id.log_text);

        // Authenticate account
        btn_login.setOnClickListener(view -> {
            ShowToast.unshowProgressBar(progressBar, loginText, timeDelayInMillis);

            String usernameOrEmail = String.valueOf(editLoginAcc.getText());
            String password = String.valueOf(editLoginPass.getText());

            // Check if username/email and password are empty
            if (TextUtils.isEmpty(usernameOrEmail)) {
                ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Enter your Username or Email to log in.", timeDelayInMillis);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Enter your password to log in.", timeDelayInMillis);
                return;
            }

            // Check if input is an email or username
            if (usernameOrEmail.contains("@")) {
                // Input is an email
                loginWithEmail(usernameOrEmail, password);
            } else {
                // Input is a username
                getEmailFromUsernameAndLogin(usernameOrEmail, password);
            }
        });

        // Redirect to forgot password
        txtForgotPass.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
            startActivity(intent);
            finish();
        });

        // Redirect to create account
        txtCreateAcc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Signup.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ShowToast.showDelayedToast(Login.this, progressBar, loginText, "LOGIN SUCCESSFUL.", timeDelayInMillis);
                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        handleLoginFailure(task.getException());
                    }
                });
    }

    private void getEmailFromUsernameAndLogin(String username, String password) {
        db.collection("users")
                .whereEqualTo("username", username) // Query for username
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String email = document.getString("email");
                            if (email != null) {
                                // Proceed with email login
                                loginWithEmail(email, password);
                            } else {
                                ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Email not found for this username.", timeDelayInMillis);
                            }
                        } else {
                            ShowToast.showDelayedToast(Login.this, progressBar, loginText, "This account does not exist.", timeDelayInMillis);
                        }
                    } else {
                        ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Failed to retrieve email.", timeDelayInMillis);
                    }
                });
    }

    private void handleLoginFailure(Exception exception) {
        try {
            throw Objects.requireNonNull(exception);
        } catch (FirebaseAuthInvalidUserException e) {
            ShowToast.showDelayedToast(Login.this, progressBar, loginText, "This account does not exist.", timeDelayInMillis);
        } catch (FirebaseAuthInvalidCredentialsException e) {
            ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Incorrect password.", timeDelayInMillis);
        } catch (Exception e) {
            ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Login failed: " + e.getMessage(), timeDelayInMillis);
        }
    }
}
