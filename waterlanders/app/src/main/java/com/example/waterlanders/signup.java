package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class signup extends AppCompatActivity {

    TextInputEditText edit_reg_email, edit_reg_fullName, edit_reg_username, edit_reg_pass, edit_reg_address;
    ProgressBar progressBar;
    Button register_button;
    FirebaseAuth mAuth;
    TextView txt_login_acc;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // init obj
        edit_reg_email = findViewById(R.id.register_email);
        edit_reg_fullName = findViewById(R.id.register_fullName);
        edit_reg_username = findViewById(R.id.register_username);
        edit_reg_pass = findViewById(R.id.register_password);
        edit_reg_address = findViewById(R.id.register_address);
        progressBar = findViewById(R.id.progress_bar);
        register_button = findViewById(R.id.register_btn);
        txt_login_acc = findViewById(R.id.login_account);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //authenticate and save to firebase
        register_button.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, fullName, username, cpNum, password, address;
            email = String.valueOf(edit_reg_email.getText());
            fullName = String.valueOf(edit_reg_fullName.getText());
            username = String.valueOf(edit_reg_username.getText());
            password = String.valueOf(edit_reg_pass.getText());
            address = String.valueOf(edit_reg_address.getText());

            // check if credentials are empty
            if (TextUtils.isEmpty(email)){
                Toast.makeText(signup.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(fullName)){
                Toast.makeText(signup.this, "Enter Full Name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(username)){
                Toast.makeText(signup.this, "Enter Username", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)){
                Toast.makeText(signup.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(address)){
                Toast.makeText(signup.this, "Enter Address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Query Firestore to check if email or username exists
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Email already exists
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(signup.this, "Email already exists. Please use a different email.", Toast.LENGTH_SHORT).show();
                        } else {
                            db.collection("users")
                                    .whereEqualTo("username", username)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                            // Username already exists
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(signup.this, "Username already exists. Please use a different username.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Email and Username are unique, proceed to create user
                                            createUser(email, password, fullName, username, address);
                                        }
                                    });
                        }
                    });

        });

        // redirect to login
        txt_login_acc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), login.class);
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
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Get the user ID of the newly created user
                            String userId = Objects.requireNonNull(task.getResult().getUser()).getUid();

                            // Set the document ID to be the user ID
                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(signup.this, "ACCOUNT CREATED SUCCESSFULLY.",
                                                    Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(getApplicationContext(), login.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(signup.this, "ERROR CREATING AN ACCOUNT.\n" + e,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(signup.this, "ERROR CREATING AN ACCOUNT.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}