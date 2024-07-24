package LoginDirectory;

import android.content.Context;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import MessageToast.ShowToast;

public class GetEmailFromUsername {
    private final Context context;
    private final TextInputEditText editLoginAcc;
    private final TextInputEditText editLoginPass;
    private final ProgressBar progressBar;
    private final TextView loginText;
    private final int timeDelayInMillis = 500;
    private final FirebaseFirestore db;

    public GetEmailFromUsername(Context context, TextInputEditText editLoginAcc, TextInputEditText editLoginPass, ProgressBar progressBar, TextView loginText) {
        this.context = context;
        this.editLoginAcc = editLoginAcc;
        this.editLoginPass = editLoginPass;
        this.progressBar = progressBar;
        this.loginText = loginText;
        this.db = FirebaseFirestore.getInstance();
    }

    public void getEmailFromUsernameAndLogin(String username, String password) {
        db.collection("users")
                .whereEqualTo("username", username) // Query for username
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String email = document.getString("email");
                            if (email != null) {
                                // Proceed with email login
                                LoginWithEmail loginWithEmail = new LoginWithEmail(context, editLoginAcc, editLoginPass, progressBar, loginText);
                                loginWithEmail.loginWithEmail(email, password);
                            } else {
                                ShowToast.showDelayedToast(context, progressBar, loginText, "Email not found for this username.", timeDelayInMillis);
                            }
                        } else {
                            ShowToast.showDelayedToast(context, progressBar, loginText, "This account does not exist.", timeDelayInMillis);
                        }
                    } else {
                        ShowToast.showDelayedToast(context, progressBar, loginText, "Failed to retrieve email.", timeDelayInMillis);
                    }
                });
    }
}
