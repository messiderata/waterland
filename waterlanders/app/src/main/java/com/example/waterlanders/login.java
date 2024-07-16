package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
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


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class login extends AppCompatActivity {

    TextInputEditText edit_login_acc, edit_login_pass;
    ProgressBar progressBar;
    Button btn_login;
    FirebaseAuth mAuth;
    TextView txt_forgot_pass, txt_create_acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        // init obj
        edit_login_acc = findViewById(R.id.login_account);
        edit_login_pass = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.progress_bar);
        btn_login = findViewById(R.id.login_button);
        mAuth = FirebaseAuth.getInstance();
        txt_forgot_pass = findViewById(R.id.forgot_password);
        txt_create_acc = findViewById(R.id.create_account);


        // authenticate account
        btn_login.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = String.valueOf(edit_login_acc.getText());
            password = String.valueOf(edit_login_pass.getText());

            // check if email and password are empty
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btn_login.setBackgroundTintList(ContextCompat.getColorStateList(login.this, R.color.white)); // Reset color

                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(login.this, "LOGIN SUCCESSFULLY.",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), home_page.class);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidUserException e) {
                                Toast.makeText(login.this, "This account does not exist.", Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(login.this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}