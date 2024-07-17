package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class Login extends AppCompatActivity {

    TextInputEditText editLoginAcc, editLoginPass;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    TextView txtForgotPass, txtCreateAcc, loginText;

    int timeDelayInMillis = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        // init obj
        editLoginAcc = findViewById(R.id.login_account);
        editLoginPass = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.progress_bar);
        CardView btn_login = findViewById(R.id.login_button);
        mAuth = FirebaseAuth.getInstance();
        txtForgotPass = findViewById(R.id.forgot_password);
        txtCreateAcc = findViewById(R.id.create_account);
        loginText = findViewById(R.id.log_text);

        // authenticate account
        btn_login.setOnClickListener(view -> {

            ShowToast.unshowProgressBar(progressBar, loginText, timeDelayInMillis);
            String email, password;
            email = String.valueOf(editLoginAcc.getText());
            password = String.valueOf(editLoginPass.getText());

            // check if email and password are empty
            if (TextUtils.isEmpty(email)) {
                ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Enter your Username or Email to log in.",timeDelayInMillis);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Enter your password to log in.",timeDelayInMillis);
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ShowToast.showDelayedToast(Login.this, progressBar, loginText, "LOGIN SUCCESSFULLY.",timeDelayInMillis);
                            Intent intent = new Intent(getApplicationContext(), HomePage.class);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidUserException e) {
                                ShowToast.showDelayedToast(Login.this, progressBar, loginText, "This account does not exist.",timeDelayInMillis);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                progressBar.setVisibility(View.VISIBLE);
                                loginText.setVisibility(View.GONE);
                                ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Incorrect password.",timeDelayInMillis);
                            } catch (Exception e) {
                                ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Login failed.",timeDelayInMillis);
                            }
                        }
                    });
        });

        // redirect to forgot password
        txtForgotPass.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
            startActivity(intent);
            finish();
        });

        // redirect to create account
        txtCreateAcc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Signup.class);
            startActivity(intent);
            finish();
        });
    }
}

