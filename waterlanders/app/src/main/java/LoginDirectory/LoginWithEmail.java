package LoginDirectory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.TextView;

import UserHomePageDirectory.MainDashboardUser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;
import Handler.ShowToast;

public class LoginWithEmail {
    TextInputEditText editLoginAcc, editLoginPass;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    TextView loginText;
    Context context;
    Intent intent;

    int timeDelayInMillis = 100;

    // Constructor to initialize the context and other fields
    public LoginWithEmail(Context context, TextInputEditText editLoginAcc, TextInputEditText editLoginPass, ProgressBar progressBar, TextView loginText) {
        this.context = context;
        this.editLoginAcc = editLoginAcc;
        this.editLoginPass = editLoginPass;
        this.progressBar = progressBar;
        this.loginText = loginText;
        mAuth = FirebaseAuth.getInstance();
    }
    public void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ShowToast.showDelayedToast(context, progressBar, loginText, "LOGIN SUCCESSFUL.", timeDelayInMillis);

                        // check user if admin, delivery, or customer
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null){
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(user.getUid()).get()
                                    .addOnCompleteListener(roleTask -> {
                                        if (roleTask.isSuccessful()) {
                                            DocumentSnapshot document = roleTask.getResult();
                                            if (document.exists()) {
                                                String role = document.getString("role");
                                                // Use the user's role as needed
                                                if (role != null) {
                                                    switch (role){
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
                                                            ShowToast.showDelayedToast(context, progressBar, loginText, "LOGIN SUCCESSFUL. Unknown role.", timeDelayInMillis);
                                                            return;
                                                    }
                                                } else {
                                                    ShowToast.showDelayedToast(context, progressBar, loginText, "LOGIN SUCCESSFUL. Role is null.", timeDelayInMillis);
                                                    return;
                                                }
                                                context.startActivity(intent);

                                                // Cast context to an activity and call finish if context is an activity
                                                if (context instanceof Activity) {
                                                    ((Activity) context).finish();
                                                }
                                            } else {
                                                ShowToast.showDelayedToast(context, progressBar, loginText, "LOGIN SUCCESSFUL. But user role not found.", timeDelayInMillis);
                                            }
                                        } else {
                                            ShowToast.showDelayedToast(context, progressBar, loginText, "LOGIN SUCCESSFUL. Failed to retrieve role.", timeDelayInMillis);
                                        }
                                    });
                        } else {
                            ShowToast.showDelayedToast(context, progressBar, loginText, "USER NOT FOUND IN DATABASE.", timeDelayInMillis);
                        }
                    } else {
                        HandleLogin handleLogin = new HandleLogin(context, progressBar, loginText, timeDelayInMillis);
                        handleLogin.handleLoginFailure(task.getException());
                    }
                });
    }


}
