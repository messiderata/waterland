package LoginDirectory;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;
import Handler.InitStrings;
import Handler.PassUtils;
import UserHomePageDirectory.MainDashboardUser;

public class LoginWithFacebook extends Login {

    CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        callbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // this part of the code we initialize the facebook login function
        // where in we get the user's public email and public_profile, specifically user name or display name
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d("FACEBOOOK", "Facebook login successful. AccessToken: " + loginResult.getAccessToken().getToken());
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d("FACEBOOOK", "Facebook login canceled by user.");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.e("FACEBOOOK", "Facebook login error: " + exception.getMessage());
                    }
                });
    }

    // this part of the code handles the callback or the result
    // base on the facebook query that we made
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // this method handles the logging-in to our app
    // base on the credential we got on our facebook query
    // then we create either new account or sign in the user
    // base on the facebook credential
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("FACEBOOOK", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    Log.d("FACEBOOOK", "signInWithCredential:success. User ID: " + (user != null ? user.getUid() : "null"));
                    if (user != null) {
                        checkIfUserIsNew(user);
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FACEBOOOK", "signInWithCredential:failure", task.getException());
                    Toast.makeText(LoginWithFacebook.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    // in this method we check if the user is existing user or not
    // if user is existing user then we just redirect the user base on their role
    // else we create a new account by passing the public email and display name
    // we got from out facebook query
    private void checkIfUserIsNew(FirebaseUser user) {
        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // User already exists, no need to create a new document
                    Log.d(TAG, "User already exists: " + user.getUid());
                    redirectUser(user);
                } else {
                    // User is new, create a new document in Firestore
                    // saveNewUser(user);
                    String initString = InitStrings.generateRandomString(10);
                    String hashedPassword = PassUtils.hashPassword(initString);
                    user.updatePassword(hashedPassword);
                    Intent showAdditionalInfoIntent = new Intent(LoginWithFacebook.this, LoginWithProviderAdditionalInfo.class);
                    showAdditionalInfoIntent.putExtra("user_email", user.getEmail());
                    showAdditionalInfoIntent.putExtra("user_fullName", user.getDisplayName());
                    showAdditionalInfoIntent.putExtra("hashedPassword", hashedPassword);
                    startActivity(showAdditionalInfoIntent);
                }
            } else {
                Log.d(TAG, "Failed to check if user is new: ", task.getException());
            }
        });
    }

    // this method is same as before where we redirect the user base
    // on the current user's role
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
                                intent = new Intent(this, AdminHomePage.class);
                                break;
                            case "DELIVERY":
                                intent = new Intent(this, DeliveryHomePage.class);
                                break;
                            case "customer":
                                intent = new Intent(this, MainDashboardUser.class);
                                break;
                            default:
                                Toast.makeText(this, "LOGIN SUCCESSFUL. Unknown role.", Toast.LENGTH_SHORT).show();
                                return;
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "LOGIN SUCCESSFUL. Role is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "LOGIN SUCCESSFUL. Failed to retrieve role.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}