package LoginDirectory;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import Handler.PassUtils;
import Handler.StatusBarUtil;
import UserHomePageDirectory.MainDashboardUser;

import com.example.waterlanders.NotificationService;
import com.example.waterlanders.R;
import ForgotPasswordDirectory.ForgotPasswordHome;
import SignUpDirectory.UserSignUp;

import com.example.waterlanders.activity.BaseActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;
import Handler.ShowToast;

public class Login extends BaseActivity {

    private TextInputEditText loginAccount;
    private TextInputEditText loginPassword;
    private ProgressBar progressBar;

    private TextView forgotPassword;
    private TextView createAccount;
    private TextView loginText;
    private ImageView facebookLogin;
    private ImageView googleLogin;
    private CardView loginButton;


    private LoginWithGoogle loginWithGoogle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int timeDelayInMillis = 1;
    private static final int RC_SIGN_IN = 9001;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        initializeObjects();
        getPermission();
        performFirebaseQuery();
    }

    // initialize objects
    private void initializeObjects(){
        loginWithGoogle = new LoginWithGoogle(this, this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // login screen objects
        loginAccount = findViewById(R.id.login_account);
        loginPassword = findViewById(R.id.login_password);
        forgotPassword = findViewById(R.id.forgot_password);
        createAccount = findViewById(R.id.create_account);
        loginText = findViewById(R.id.log_text);
        progressBar = findViewById(R.id.progress_bar);
        loginButton = findViewById(R.id.login_button);
        facebookLogin = findViewById(R.id.facebook_login);
        googleLogin = findViewById(R.id.google_login);
    }

    // Check if the user is already logged in
    // If the user is logged in, redirect them based on their role
    // If the user is not logged in, show the login screen
    private void performFirebaseQuery() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        runOnUiThread(() -> {
            if (firebaseUser != null) {
                redirectUser(firebaseUser);
            } else {
                initializeLoginScreen();
            }
        });
    }

    // Set up login button click listeners
    // users may login using their existing account by
    // logging in their email and password
    // or using facebook or google
    // if user login tru facebook or google
    // a new account will automatically created
    // users may create their account manually as well
    // if the user forgot their password
    // they may reset it as well
    private void initializeLoginScreen() {
        loginButton.setOnClickListener(view -> {
            ShowToast.unshowProgressBar(progressBar, loginText, timeDelayInMillis);
            String usernameOrEmail = Objects.requireNonNull(loginAccount.getText()).toString().trim();
            String password = Objects.requireNonNull(loginPassword.getText()).toString().trim();
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
                LoginWithEmail loginWithEmail = new LoginWithEmail(this, loginAccount, loginPassword, progressBar, loginText);
                loginWithEmail.loginWithEmail(usernameOrEmail, password);
            } else {
                // Input is a username
                GetEmailFromUsername getEmailFromUsername = new GetEmailFromUsername(this, loginAccount, loginPassword, progressBar, loginText);
                getEmailFromUsername.getEmailFromUsernameAndLogin(usernameOrEmail, password);
            }
        });

        // Set up clickable text for forgot password and create account
        forgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPasswordHome.class);
            startActivity(intent);
        });

        createAccount.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UserSignUp.class);
            startActivity(intent);
        });

        facebookLogin.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, LoginWithFacebook.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });

        googleLogin.setOnClickListener(view -> {
            loginWithGoogle.signIn();
        });
    }

    // this method is to initialize the logging in tru google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from the Google sign-in activity
        if (requestCode == RC_SIGN_IN) {
            loginWithGoogle.handleSignInResult(data);
        }
    }

    // this method is responsible to handle the redirection of pages
    // base on the current user role
    // if the current user is 'customer' then redirect to the MainDashboardUser
    // if the current user is 'DELIVERY' then redirect to the DeliveryHomePage
    // if the current user is 'ADMIN' then redirect to the AdminHomePage
    private void redirectUser(FirebaseUser firebaseUser) {
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

    // this method is responsible to ask permission to the user
    // for the app send sms to the device
    private void getPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

    }

    // this method is just to show which action that the user take
    // if user grant the permission or not
    // it will display corresponding text response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS Permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "SMS Permission denied. The app will not be able to send OTP.", Toast.LENGTH_LONG).show();
            }
        }
    }
}