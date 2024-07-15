package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class validate_otp extends AppCompatActivity {

    Long timeoutSeconds = 60L;
    TextInputEditText edit_otp;
    Button btn_submit, btn_back;
    ProgressBar progressBar;
    String input_details, verificationCode, currUserID;
    TextView resendOtpTextView;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    PhoneAuthProvider.ForceResendingToken  resendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_validate_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edit_otp = findViewById(R.id.otp_otp);
        resendOtpTextView = findViewById(R.id.resend_otp_textview);
        btn_submit = findViewById(R.id.submit_btn);
        btn_back = findViewById(R.id.back_btn);
        progressBar = findViewById(R.id.progress_bar);

        input_details = getIntent().getStringExtra("input_details");
        currUserID = getIntent().getStringExtra("document_id");

        sendOTP(input_details, false);

        resendOtpTextView.setOnClickListener((v)->{
            sendOTP(input_details,true);
        });

        btn_submit.setOnClickListener(view -> {
            String enteredOtp  = Objects.requireNonNull(edit_otp.getText()).toString();
            PhoneAuthCredential credential =  PhoneAuthProvider.getCredential(verificationCode,enteredOtp);
            signIn(credential);
        });

        // back to phone number email prompt
        btn_back.setOnClickListener(view -> {
            Intent intent = new Intent(validate_otp.this, forgot_password.class);
            startActivity(intent);
            finish();
        });
    }

    void sendOTP(String phoneNumber,boolean isResend){
        startResendTimer();
        setInProgress(true);
        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            signIn(phoneAuthCredential);
                            setInProgress(false);
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Toast.makeText(validate_otp.this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                            setInProgress(false);
                        }

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(s, forceResendingToken);
                            verificationCode = s;
                            resendingToken = forceResendingToken;
                            Toast.makeText(validate_otp.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                            setInProgress(false);
                        }
                    });
        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }else{
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }

    }

    void setInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            btn_submit.setVisibility(View.VISIBLE);
        }
    }

    void signIn(PhoneAuthCredential phoneAuthCredential){
        //login and go to next activity
        setInProgress(true);
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if(task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    Log.d("Firestore", "user: " + Objects.requireNonNull(user).getEmail());
                    if (user != null) {
                        Intent intent = new Intent(validate_otp.this, update_password.class);
                        intent.putExtra("currUser", user);
                        startActivity(intent);
                        finish();
                    }
                }else{
                    Toast.makeText(validate_otp.this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    void startResendTimer(){
        resendOtpTextView.setEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                resendOtpTextView.setText(MessageFormat.format("{0}{1}{2}", getString(R.string.resend_otp_in), timeoutSeconds, getString(R.string.seconds)));
                if(timeoutSeconds<=0){
                    timeoutSeconds =60L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendOtpTextView.setEnabled(true);
                    });
                }
            }
        },0,1000);
    }
}