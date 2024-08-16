package LoginDirectory;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import UserHomePageDirectory.MainDashboardUser;
import com.example.waterlanders.R;
import com.example.waterlanders.activity.ForgotPassword;
import com.example.waterlanders.activity.Signup;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
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
    private ImageView img_facebook_login, img_google_login;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    private static final int timeDelayInMillis = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use a lightweight view to avoid unnecessary delays
        EdgeToEdge.enable(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Perform Firebase query on a background thread
        performFirebaseQuery();

    }

    private void performFirebaseQuery() {
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
        img_facebook_login = findViewById(R.id.facebookLogin);
        img_google_login = findViewById(R.id.googleLogin);

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

        img_facebook_login.setOnClickListener(view -> {

        });

        img_google_login.setOnClickListener(view -> {
            signInWithGoogle();
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

    public void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkIfUserIsNew(user);
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void checkIfUserIsNew(FirebaseUser user) {
        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // User already exists, no need to create a new document
                        Log.d(TAG, "User already exists: " + user.getUid());
                        updateUI(user);
                    } else {
                        // User is new, create a new document in Firestore
                        saveNewUser(user);
                    }
                } else {
                    Log.d(TAG, "Failed to check if user is new: ", task.getException());
                }
            }
        });
    }

    private void saveNewUser(FirebaseUser user) {
        // Create a new user with the provided information
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("username", user.getDisplayName());
        userData.put("fullName", user.getDisplayName());
        userData.put("role", "customer");

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New user added successfully");
                    updateUI(user);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding new user", e);
                });
    }

    private void updateUI(FirebaseUser user) {
        // Redirect user based on their role or proceed with the app flow
        redirectUser(user);
    }
}
