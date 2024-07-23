package com.example.waterlanders.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class ForgotPassword extends AppCompatActivity {

    private EditText editTextPhone, editTextOtp, editTextNewPassword;
    private Button buttonSendOtp, buttonResetPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String verificationId;
    private TextView testLangTo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextPhone = findViewById(R.id.editTextPhone);
        editTextOtp = findViewById(R.id.editTextOtp);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        testLangTo = findViewById(R.id.walapa);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase and set emulator settings
        FirebaseApp.initializeApp(this);
        mAuth.useEmulator("10.0.2.2", 9099); // Use "10.0.2.2" for Android emulator, "localhost" for physical device

        buttonSendOtp.setOnClickListener(v -> {
            String phone = editTextPhone.getText().toString().trim();
            if (phone.isEmpty() || phone.length() != 10 || !phone.startsWith("9")) {
                editTextPhone.setError("Enter a valid phone number");
                editTextPhone.requestFocus();
            } else {
                String fullPhoneNumber = "+63" + phone;
                sendVerificationCode(fullPhoneNumber);
            }
        });

        buttonResetPassword.setOnClickListener(v -> {
            String code = editTextOtp.getText().toString().trim();
            String newPassword = editTextNewPassword.getText().toString().trim();
            if (code.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(ForgotPassword.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                verifyCodeAndUpdatePassword(code, newPassword);
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                editTextOtp.setText(code);
                verifyCodeAndUpdatePassword(code, editTextNewPassword.getText().toString().trim());
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(ForgotPassword.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            Toast.makeText(ForgotPassword.this, "OTP sent", Toast.LENGTH_LONG).show();
        }
    };

    private void verifyCodeAndUpdatePassword(String code, String newPassword) {
        if (verificationId == null) {
            Toast.makeText(ForgotPassword.this, "Verification ID not found. Please request OTP again.", Toast.LENGTH_LONG).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // Verify the OTP
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // OTP verified, now update the password
                        String phoneNumber = editTextPhone.getText().toString().trim();
                        updatePasswordInFirestore(phoneNumber, newPassword);
                    } else {
                        Toast.makeText(ForgotPassword.this, "OTP verification failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updatePasswordInFirestore(String phoneNumber, String newPassword) {
        testLangTo.setText(phoneNumber);
        db.collection("users")
                .whereEqualTo("mobile number", phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // User exists, update the password
                        DocumentReference userRef = task.getResult().getDocuments().get(0).getReference();
                        userRef.update("Password", newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ForgotPassword.this, "Password updated successfully", Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ForgotPassword.this, "Password update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Toast.makeText(ForgotPassword.this, "No user found with this phone number", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
