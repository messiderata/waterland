package ForgotPasswordDirectory;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;

public class ForgotPasswordPhoneOTP extends AppCompatActivity {

    private ImageView backIcon;
    private EditText OTPString;
    private Button confirm;
    private TextView timerTextView;

    private FirebaseAuth mAuth;
    private String phoneNumber;
    private String userEmail;
    private String userPassword;
    private int OTP;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password_phone_otp);
        initializeObjects();
        getIntentData();
        // sendVerificationCode(phoneNumber);

        // Check for SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        } else {
            sendVerificationCode(phoneNumber);
        }

        confirm.setOnClickListener(view -> verifyCode(String.valueOf(OTPString.getText())));

        backIcon.setOnClickListener(view -> finish());
    }

    private void initializeObjects(){
        backIcon = findViewById(R.id.backIcon);
        OTPString = findViewById(R.id.OTP_string);
        confirm = findViewById(R.id.confirm);
        timerTextView = findViewById(R.id.timerTextView);

        mAuth = FirebaseAuth.getInstance();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        phoneNumber = (String) intent.getSerializableExtra("phone_number");
        userEmail = (String) intent.getSerializableExtra("user_email");
        userPassword = (String) intent.getSerializableExtra("user_pass");
    }

    // Handle the permission result locally
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendVerificationCode(phoneNumber);
            } else {
                Toast.makeText(this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendVerificationCode(String phoneNumber) {
        OTP = ForgotPasswordLocalSmsService.generateFourDigitNumber();
        ForgotPasswordLocalSmsService.sendSMS(this, phoneNumber, "Your OTP is: "+OTP);

        // Start a 1-minute countdown timer
        countDownTimer = new CountDownTimer(60000, 1000) { // 60000 milliseconds = 1 minute, 1000 milliseconds = 1 second interval
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the TextView with the remaining time
                timerTextView.setText("Time remaining: " + millisUntilFinished / 1000 + " seconds");
            }

            @Override
            public void onFinish() {
                // Finish the current activity after 1 minute
                Toast.makeText(ForgotPasswordPhoneOTP.this, "Time expired. Please try again.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }.start();
    }

    private void verifyCode(String code){
        if (code.equals(String.valueOf(OTP))){
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            Intent setNewPasswordIntent = new Intent(this, ForgotPasswordPhoneNewPassword.class);
            setNewPasswordIntent.putExtra("user_email", userEmail);
            setNewPasswordIntent.putExtra("user_pass", userPassword);
            startActivity(setNewPasswordIntent);
            finish();
        } else {
            Toast.makeText(ForgotPasswordPhoneOTP.this, "Verification failed", Toast.LENGTH_SHORT).show();
        }
    }
}