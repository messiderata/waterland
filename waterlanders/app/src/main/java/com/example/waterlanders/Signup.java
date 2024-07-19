package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.cardview.widget.CardView;


import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    TextInputEditText editRegEmail, editRegFullName, editRegUsername, editRegPass, editRegAddress;
    FirebaseAuth mAuth;
    TextView txtLoginAcc, signupText;
    FirebaseFirestore db;
    ProgressBar progressBar;

    int timeDelayInMillis = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // init obj
        editRegEmail = findViewById(R.id.register_email);
        editRegFullName = findViewById(R.id.register_fullName);
        editRegUsername = findViewById(R.id.register_username);
        editRegPass = findViewById(R.id.register_password);
        editRegAddress = findViewById(R.id.register_address);
        progressBar = findViewById(R.id.progress_bar);
        CardView register_button = findViewById(R.id.registerbtn);
        signupText = findViewById(R.id.singup_text);
        txtLoginAcc = findViewById(R.id.login_account);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //authenticate and save to firebase
        register_button.setOnClickListener(view -> {
            ShowToast.unshowProgressBar(progressBar, signupText, timeDelayInMillis);
            String email, fullName, username, password, address;
            email = String.valueOf(editRegEmail.getText());
            fullName = String.valueOf(editRegFullName.getText());
            username = String.valueOf(editRegUsername.getText());
            password = String.valueOf(editRegPass.getText());
            address = String.valueOf(editRegAddress.getText());

            // check if credentials are empty

            if (TextUtils.isEmpty(email)) {
                ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "Enter your email.", timeDelayInMillis);
                return;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "Invalid email address.", timeDelayInMillis);
                return;
            } else if (TextUtils.isEmpty(fullName)) {
                ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "Enter your full name.", timeDelayInMillis);
                return;
            } else if (TextUtils.isEmpty(username)) {
                ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "Enter your username.", timeDelayInMillis);
                return;
            } else if (TextUtils.isEmpty(password)) {
                ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "Enter your password.", timeDelayInMillis);
                return;
            } else if (TextUtils.isEmpty(address)) {
                ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "Enter your address.", timeDelayInMillis);
                return;
            }


            // Query Firestore to check if email or username exists
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Email already exists
                            ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "Email already exists. Please use a different email.",timeDelayInMillis);

                        } else {
                            db.collection("users")
                                    .whereEqualTo("username", username)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                            // Username already exists
                                            ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "Username already exists. Please use a different username.",timeDelayInMillis);

                                        } else {
                                            // Email and Username are unique, proceed to create user
                                            createUser(email, password, fullName, username, address);
                                        }
                                    });
                        }
                    });

        });

        // redirect to login
        txtLoginAcc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void createUser(String email, String password, String fullName, String username, String address) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("fullName", fullName);
        user.put("username", username);
        user.put("address", address);
        user.put("role", "customer");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the user ID of the newly created user
                        String userId = Objects.requireNonNull(task.getResult().getUser()).getUid();

                        // Set the document ID to be the user ID
                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "ACCOUNT CREATED SUCCESSFULLY.",timeDelayInMillis);
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(Signup.this, "ERROR CREATING AN ACCOUNT.\n" + e,
                                        Toast.LENGTH_SHORT).show());

                    } else {
                        ShowToast.showDelayedToast(Signup.this, progressBar, signupText, "ERROR CREATING AN ACCOUNT.",timeDelayInMillis);

                    }
                });

    }

}