package ForgotPasswordDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordEmail extends AppCompatActivity {

    private ImageView backIcon;
    private EditText emailField;
    private Button sendButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_email);
        initializeObjects();

        sendButton.setOnClickListener(view -> {
            checkInputData();
        });

        backIcon.setOnClickListener(view -> {
            finish();
        });

    }

    private void initializeObjects(){
        backIcon = findViewById(R.id.backIcon);
        emailField = findViewById(R.id.emailField);
        sendButton = findViewById(R.id.sendButton);

        auth = FirebaseAuth.getInstance();
    }

    private void checkInputData(){
        boolean isValid = true;
        if (TextUtils.isEmpty(emailField.getText())){
            isValid = false;
            Toast.makeText(this, "Input your Email first.", Toast.LENGTH_SHORT).show();
        } else if (!isEmailValid(String.valueOf(emailField.getText()))) {
            isValid = false;
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
        }

        // if email is valid then send the reset link tru email
        if (isValid){
            sendPasswordResetEmail(String.valueOf(emailField.getText()));
        }
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendPasswordResetEmail(String email){
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent backIntent = new Intent(ForgotPasswordEmail.this, ForgotPasswordSuccess.class);
                        backIntent.putExtra("success_message","Password Reset Link Sent");
                        backIntent.putExtra("success_description","The reset link is sent to your email.");
                        startActivity(backIntent);
                    }
                }
            });
    }
}
