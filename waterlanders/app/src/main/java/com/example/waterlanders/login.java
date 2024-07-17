package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
// Import Handler class
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.os.Handler; // Import Handler class
import android.os.Looper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class login extends AppCompatActivity {

    TextInputEditText edit_login_acc, edit_login_pass;
    ProgressBar progress_bar;
    Button btn_login;
    FirebaseAuth mAuth;
    TextView txt_forgot_pass, txt_create_acc,login_text ;

    int timeDelayInMillis = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        // init obj
        edit_login_acc = findViewById(R.id.login_account);
        edit_login_pass = findViewById(R.id.login_password);
        progress_bar = findViewById(R.id.progress_bar);
        CardView btn_login = findViewById(R.id.login_button);
        mAuth = FirebaseAuth.getInstance();
        txt_forgot_pass = findViewById(R.id.forgot_password);
        txt_create_acc = findViewById(R.id.create_account);
        login_text = findViewById(R.id.log_text);

        // authenticate account
        btn_login.setOnClickListener(view -> {

            UnshowProgressBar();

            String email, password;
            email = String.valueOf(edit_login_acc.getText());
            password = String.valueOf(edit_login_pass.getText());

            // check if email and password are empty
            if (TextUtils.isEmpty(email)) {
                showToast.showDelayedToast(login.this, progress_bar, login_text, "Enter your Username or Email to log in.");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                showToast.showDelayedToast(login.this, progress_bar, login_text, "Enter your password to log in.");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast.showDelayedToast(login.this, progress_bar, login_text, "LOGIN SUCCESSFULLY.");
                            Intent intent = new Intent(getApplicationContext(), home_page.class);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidUserException e) {
                                showToast.showDelayedToast(login.this, progress_bar, login_text, "This account does not exist.");
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                progress_bar.setVisibility(View.VISIBLE);
                                login_text.setVisibility(View.GONE);
                                showToast.showDelayedToast(login.this, progress_bar, login_text, "Incorrect password.");
                            } catch (Exception e) {
                                showToast.showDelayedToast(login.this, progress_bar, login_text, "Login failed.");
                            }
                        }
                    });
        });

        // redirect to forgot password
        txt_forgot_pass.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), forgot_password.class);
            startActivity(intent);
            finish();
        });

        // redirect to create account
        txt_create_acc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), signup.class);
            startActivity(intent);
            finish();
        });
    }

    private void UnshowProgressBar() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.INVISIBLE);
                login_text.setVisibility(View.VISIBLE);
            }
        },timeDelayInMillis); // Delay for 1 second (1000 milliseconds)
    }
}

