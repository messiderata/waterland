package LoginDirectory;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import HomePageDirectory.HomePage;
import MessageToast.ShowToast;



public class LoginWithEmail {
    TextInputEditText editLoginAcc, editLoginPass;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    TextView loginText;
    Context context;

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
                        Intent intent = new Intent(context, HomePage.class);
                        context.startActivity(intent);

                        // Cast context to an activity and call finish if context is an activity
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    } else {
                        HandleLogin handleLogin = new HandleLogin(context, progressBar, loginText, timeDelayInMillis);
                        handleLogin.handleLoginFailure(task.getException());
                    }
                });
    }


}
