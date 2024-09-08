package ForgotPasswordDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ForgotPasswordPhoneOTP extends AppCompatActivity {

    private ImageView backIcon;
    private EditText OTPString;
    private Button confirm;

    private FirebaseAuth mAuth;
    private String phoneNumber;
    private String userEmail;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_phone_otp);
        initializeObjects();
        getIntentData();
        sendVerificationCode(phoneNumber);

        confirm.setOnClickListener(view -> verifyCode(String.valueOf(OTPString.getText())));

        backIcon.setOnClickListener(view -> finish());
    }

    private void initializeObjects(){
        backIcon = findViewById(R.id.backIcon);
        OTPString = findViewById(R.id.OTP_string);
        confirm = findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        phoneNumber = (String) intent.getSerializableExtra("phone_number");
        userEmail = (String) intent.getSerializableExtra("user_email");
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            String code = credential.getSmsCode();
            if (code != null) {
                OTPString.setText(code);  // Auto-fill the code
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(ForgotPasswordPhoneOTP.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ForgotPasswordPhone", "Verification failed", e);
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(s, token);
            verificationId = s;
            Toast.makeText(ForgotPasswordPhoneOTP.this, "OTP sent to your phone", Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String code) {
        if (verificationId == null) {
            Toast.makeText(this, "Please request a new verification code", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent setNewPasswordIntent = new Intent(this, ForgotPasswordPhoneNewPassword.class);
                    startActivity(setNewPasswordIntent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordPhoneOTP.this, "Verification failed", Toast.LENGTH_SHORT).show();
                }
            });
    }
}