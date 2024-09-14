package ForgotPasswordDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import Handler.PassUtils;


public class ForgotPasswordPhoneNewPassword extends AppCompatActivity {

    private EditText newP;
    private EditText conP;
    private Button confirm;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userEmail;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password_phone_new_password);
        initializeObjects();
        getIntentData();

        confirm.setOnClickListener(view -> checkInputData());
    }

    private void initializeObjects(){
        newP = findViewById(R.id.newP);
        conP = findViewById(R.id.conP);
        confirm = findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        userEmail = (String) intent.getSerializableExtra("user_email");
        userPassword = (String) intent.getSerializableExtra("user_pass");
    }

    private void checkInputData(){
        boolean isValid = true;
        if (TextUtils.isEmpty(newP.getText())){
            isValid = false;
            Toast.makeText(this, "Input your Password first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(conP.getText())){
            isValid = false;
            Toast.makeText(this, "Confirm your Password first.", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(newP.getText()).length() < 8) {
            isValid = false;
            Toast.makeText(this, "Password must be 8 characters and above.", Toast.LENGTH_SHORT).show();
        } else if (!String.valueOf(newP.getText()).equals(String.valueOf(conP.getText()))){
            isValid = false;
            Toast.makeText(this, "Password not match.", Toast.LENGTH_SHORT).show();
        }

        if (isValid){
            updatePassword(String.valueOf(newP.getText()));
        }
    }

    private void updatePassword(String newPassword) {
        // sign in the user base on the previous user credentials
        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String hashedPassword = PassUtils.hashPassword(newPassword);
                        user.updatePassword(hashedPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // update the user pass in database
                                        String userId = user.getUid();
                                        db.collection("users").document(userId)
                                            .update("password", hashedPassword)
                                            .addOnSuccessListener(aVoid -> {
                                                Intent backIntent = new Intent(ForgotPasswordPhoneNewPassword.this, ForgotPasswordSuccess.class);
                                                backIntent.putExtra("success_message","Password Reset Successfully");
                                                backIntent.putExtra("success_description","You successfully updated your password");
                                                startActivity(backIntent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(ForgotPasswordPhoneNewPassword.this, "ERROR CREATING AN ACCOUNT.\n" + e,
                                                    Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(ForgotPasswordPhoneNewPassword.this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    } else {
                        Toast.makeText(ForgotPasswordPhoneNewPassword.this, "USER NOT FOUND IN DATABASE.", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Log.d("FORGOT PASSWORD PHONE NEW PASSWORD", "Failed to re-authenticate user: " + task.getException().getMessage());
                    Toast.makeText(ForgotPasswordPhoneNewPassword.this, "Failed to re-authenticate user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}