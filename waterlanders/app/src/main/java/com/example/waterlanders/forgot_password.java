package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class forgot_password extends AppCompatActivity {

    TextInputEditText edit_fgt_email_phone;
    Button btn_send, btn_back;
    ProgressBar progressBar;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edit_fgt_email_phone = findViewById(R.id.fgt_email_phone);
        btn_send = findViewById(R.id.fgt_send_code);
        btn_back = findViewById(R.id.fgt_back);
        progressBar = findViewById(R.id.progress_bar);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // do something
        btn_send.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String input_details = String.valueOf(edit_fgt_email_phone.getText());
            Log.d("input_details", "input_details: " + input_details);

            // check if credentials are empty
            if (TextUtils.isEmpty(input_details)){
                Toast.makeText(forgot_password.this, "Enter Details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine if input_details is email or phone number
            if (isValidEmail(input_details)){
                db.collection("users")
                        .whereEqualTo("email", input_details)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                resetPassword(input_details);
                            } else {
                                Toast.makeText(forgot_password.this, "Email does not exist.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (isValidPhoneNumber(input_details)) {
                db.collection("users")
                    .whereEqualTo("cellphone", input_details)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean exists = false;
                                String documentId = null;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    exists = true;
                                    documentId = document.getId();
                                    break;
                                }
                                if (exists) {
                                    // The inputDetails exist in Firestore
                                    Log.d("Firestore", "Document exists");
                                    Log.d("Firestore", "input_details: " + input_details);
                                    Log.d("Firestore", "documentId: " + documentId);
                                    Intent intent = new Intent(forgot_password.this, validate_otp.class);
                                    intent.putExtra("input_details", input_details);
                                    intent.putExtra("document_id", documentId);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // The inputDetails do not exist in Firestore
                                    Log.d("Firestore", "Document does not exist");
                                    Toast.makeText(forgot_password.this, "Cellphone Number does not belong to any users.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d("Firestore", "Error getting documents: ", task.getException());
                                Toast.makeText(forgot_password.this, "Unexpected Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            } else {
                Toast.makeText(forgot_password.this, "Invalid email or phone number", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }

        });

        // redirect to login
        btn_back.setOnClickListener(view -> {
            Intent intent = new Intent(forgot_password.this, login.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // You can implement more robust phone number validation as needed
        return Patterns.PHONE.matcher(phoneNumber).matches();
    }

    private void resetPassword(String input_details){
        mAuth.sendPasswordResetEmail(input_details)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(forgot_password.this, "Reset Password link has been sent to your registered Email", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(forgot_password.this, login.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(forgot_password.this, "Error :- " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }
}