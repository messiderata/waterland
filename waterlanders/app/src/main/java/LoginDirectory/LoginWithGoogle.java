package LoginDirectory;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.waterlanders.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;
import UserHomePageDirectory.MainDashboardUser;

public class LoginWithGoogle {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Context context;
    private Activity activity;

    public LoginWithGoogle(Activity activity, Context context) {
        this.context = context;
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d("IS THIS WORKING?", "1a");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void signIn() {
        Log.d("IS THIS WORKING?", "sign in 2222");
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    public void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                Log.d("IS THIS WORKING?", "fab aut");
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            Log.w("IS THIS WORKING?", "Google sign in failed", e);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("IS THIS WORKING?", "idToken: "+idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d("IS THIS WORKING?", "checking");
                            checkIfUserIsNew(user);
                        }
                    } else {
                        Log.w("IS THIS WORKING?", "signInWithCredential:failure", task.getException());
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
                        Log.d("IS THIS WORKING?", "User already exists: " + user.getUid());
                        redirectUser(user);
                    } else {
                        // User is new, create a new document in Firestore
                        // saveNewUser(user);
                        Log.d("IS THIS WORKING?", "intenting");
                        Intent showAdditionalInfoIntent = new Intent(context, LoginWithProviderAdditionalInfo.class);
                        showAdditionalInfoIntent.putExtra("user_email", user.getEmail());
                        showAdditionalInfoIntent.putExtra("user_fullName", user.getDisplayName());
                        context.startActivity(showAdditionalInfoIntent);
                    }
                } else {
                    Log.d(TAG, "Failed to check if user is new: ", task.getException());
                }
            }
        });
    }

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
                                intent = new Intent(context, AdminHomePage.class);
                                break;
                            case "DELIVERY":
                                intent = new Intent(context, DeliveryHomePage.class);
                                break;
                            case "customer":
                                intent = new Intent(context, MainDashboardUser.class);
                                break;
                            default:
                                Toast.makeText(context, "LOGIN SUCCESSFUL. Unknown role.", Toast.LENGTH_SHORT).show();
                                return;
                        }
                        activity.startActivity(intent);
                        activity.finish();
                    } else {
                        Toast.makeText(context, "LOGIN SUCCESSFUL. Role is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "LOGIN SUCCESSFUL. Failed to retrieve role.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
