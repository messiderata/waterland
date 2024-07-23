package LoginDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.waterlanders.R;
import com.example.waterlanders.activity.ForgotPassword;
import com.example.waterlanders.activity.Signup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import HomePageDirectory.HomePage;
import MessageToast.ShowToast;

public class Login extends AppCompatActivity {

    TextInputEditText editLoginAcc, editLoginPass;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView txtForgotPass, txtCreateAcc, loginText;

    int timeDelayInMillis = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize objects
        editLoginAcc = findViewById(R.id.login_account);
        editLoginPass = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.progress_bar);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        txtForgotPass = findViewById(R.id.forgot_password);
        txtCreateAcc = findViewById(R.id.create_account);
        loginText = findViewById(R.id.log_text);

        // Authenticate account
        checkFirebaseUser ();
        ClickableText();
    }

    public void checkFirebaseUser () {
        FirebaseUser firebaseUser = mAuth.getCurrentUser ();
        CardView btn_login = findViewById(R.id.login_button);

        if (firebaseUser != null) {
            //User is logged in already. You can proceed with your next screen
            Intent intent = new Intent(getApplicationContext(), HomePage.class);
            startActivity(intent);
            finish();
        } else {
            //User not logged in. So show email and password edit text for user to enter credentials
            btn_login.setOnClickListener(view -> {
                ShowToast.unshowProgressBar(progressBar, loginText, timeDelayInMillis);
                String usernameOrEmail = String.valueOf(editLoginAcc.getText());
                String password = String.valueOf(editLoginPass.getText());

                // Check if username/email and password are empty
                if (TextUtils.isEmpty(usernameOrEmail)) {
                    ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Enter your Username or Email to log in.", timeDelayInMillis);
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    ShowToast.showDelayedToast(Login.this, progressBar, loginText, "Enter your password to log in.", timeDelayInMillis);
                    return;
                }

                // Check if input is an email or username
                if (usernameOrEmail.contains("@")) {
                    // Input is an email
                    LoginWithEmail loginWithEmail = new LoginWithEmail(this, editLoginAcc, editLoginPass, progressBar, loginText);
                    loginWithEmail.loginWithEmail(usernameOrEmail, password);

                } else {
                    // Input is a username
                    GetEmailFromUsername getEmailFromUsername = new GetEmailFromUsername(this, editLoginAcc, editLoginPass, progressBar, loginText);
                    getEmailFromUsername.getEmailFromUsernameAndLogin(usernameOrEmail, password);
                }
            });
        }
    }

    private void ClickableText(){

        // Redirect to forgot password
        txtForgotPass.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
            startActivity(intent);
            finish();
        });

        // Redirect to create account
        txtCreateAcc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Signup.class);
            startActivity(intent);
            finish();
        });
    }
}
