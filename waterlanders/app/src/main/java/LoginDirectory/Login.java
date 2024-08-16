package LoginDirectory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import UserHomePageDirectory.MainDashboardUser;
import com.example.waterlanders.R;
import com.example.waterlanders.activity.ForgotPassword;
import com.example.waterlanders.activity.Signup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;
import Handler.ShowToast;

public class Login extends AppCompatActivity {

    private TextInputEditText editLoginAcc, editLoginPass;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView txtForgotPass, txtCreateAcc, loginText;

    private static final int timeDelayInMillis = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use a lightweight view to avoid unnecessary delays
        EdgeToEdge.enable(this);

        // Perform Firebase query on a background thread
        performFirebaseQuery();
    }

    private void performFirebaseQuery() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if the user is already logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        runOnUiThread(() -> {
            if (firebaseUser != null) {
                // If the user is logged in, redirect them based on their role
                redirectUser(firebaseUser);
            } else {
                // If the user is not logged in, show the login screen
                initializeLoginScreen();
            }
        });
    }

    private void initializeLoginScreen() {
        setContentView(R.layout.activity_login);

        // Initialize views
        editLoginAcc = findViewById(R.id.login_account);
        editLoginPass = findViewById(R.id.login_password);
        txtForgotPass = findViewById(R.id.forgot_password);
        txtCreateAcc = findViewById(R.id.create_account);
        loginText = findViewById(R.id.log_text);
        progressBar = findViewById(R.id.progress_bar);

        // Set up login button click listener
        CardView btn_login = findViewById(R.id.login_button);
        btn_login.setOnClickListener(view -> {
            ShowToast.unshowProgressBar(progressBar, loginText, timeDelayInMillis);
            String usernameOrEmail = Objects.requireNonNull(editLoginAcc.getText()).toString().trim();
            String password = Objects.requireNonNull(editLoginPass.getText()).toString().trim();
            hideKeyboard(this);

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

        // Set up clickable text for forgot password and create account
        txtForgotPass.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
            startActivity(intent);
            finish();
        });

        txtCreateAcc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Signup.class);
            startActivity(intent);
            finish();
        });
    }

    private void redirectUser(FirebaseUser firebaseUser) {
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.progress_bar);
        loginText = findViewById(R.id.log_text);

        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String role = document.getString("role");
                        Intent intent;
                        if (role != null) {
                            switch (role) {
                                case "ADMIN":
                                    intent = new Intent(Login.this, AdminHomePage.class);
                                    break;
                                case "DELIVERY":
                                    intent = new Intent(Login.this, DeliveryHomePage.class);
                                    break;
                                case "customer":
                                    intent = new Intent(Login.this, MainDashboardUser.class);
                                    break;
                                default:
                                    ShowToast.showDelayedToast(Login.this, progressBar, loginText, "LOGIN SUCCESSFUL. Unknown role.", timeDelayInMillis);
                                    return;
                            }
                            startActivity(intent);
                            finish(); // Close the login activity
                        } else {
                            ShowToast.showDelayedToast(Login.this, progressBar, loginText, "LOGIN SUCCESSFUL. Role is null.", timeDelayInMillis);
                        }
                    } else {
                        ShowToast.showDelayedToast(Login.this, progressBar, loginText, "LOGIN SUCCESSFUL. Failed to retrieve role.", timeDelayInMillis);
                    }
                });
        initializeLoginScreen();
    }

    public void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
