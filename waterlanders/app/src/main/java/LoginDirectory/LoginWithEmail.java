package LoginDirectory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ForgotPasswordDirectory.ForgotPasswordEmail;
import Handler.PassUtils;
import UserHomePageDirectory.MainDashboardUser;

import com.example.waterlanders.NotificationService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;
import Handler.ShowToast;

public class LoginWithEmail {
    TextInputEditText editLoginAcc, editLoginPass;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
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
        db = FirebaseFirestore.getInstance();
    }

    // flow
    // 1. since the password is encrypted, check first the user role if admin, delivery, or customer
    // 2. if admin or delivery then we don't need to convert the password base on our encryption
    // 3. else if the 'isResetPassTruEmail' field is == 1
    //      then we don't need to convert the password as well
    //      because the firebase itself handles the updating the password of the user
    // 4. else convert the password base on our encryption
    public void loginWithEmail(String email, String password) {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> usersList = task.getResult().getDocuments();

                for (DocumentSnapshot user : usersList){
                    String userEmail = (String) user.get("email");
                    String userRole = (String) user.get("role");

                    if (userEmail.equals(email)){

                        if (userRole.equals("ADMIN") || userRole.equals("DELIVERY")){
                            // don't encrypt the password
                            loginUserWithEmail(email, password, 0);
                        } else {
                            Long isResetPassTruEmailLong = (Long) user.get("isResetPassTruEmail");
                            int isResetPassTruEmail = isResetPassTruEmailLong.intValue();

                            if (isResetPassTruEmail == 1){
                                // don't encrypt pass
                                loginUserWithEmail(email, password, 1);
                            } else {
                                // encrypt pass
                                String userPass = (String) user.get("password");
                                boolean isPasswordCorrect = PassUtils.checkPassword(password, userPass);

                                if (isPasswordCorrect){
                                    loginUserWithEmail(email, userPass, 0);
                                } else {
                                    Toast.makeText(context, "Wrong password.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Failed to retrieve users data", Toast.LENGTH_SHORT).show();
                HandleLogin handleLogin = new HandleLogin(context, progressBar, loginText, timeDelayInMillis);
                handleLogin.handleLoginFailure(task.getException());
            }
        });
    }

    // flow
    // 1. check the user's role then set the intent to redirect the current user
    //      base on their role
    // 2. if the current user's role is 'customer' and isResetPassTruEmail == 1 meaning
    //      the user's current hash password is not save to the database yet
    //      therefore we will save the new hash password to the database
    //      then redirect the user to MainDashboardUser
    // 3. else if current user's role is not 'customer' then redirect the user without extra steps
    private void loginUserWithEmail(String email, String password, int isResetPassTruEmail){
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ShowToast.showDelayedToast(context, progressBar, loginText, "LOGIN SUCCESSFUL.", timeDelayInMillis);

                    // check user if admin, delivery, or customer
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null){
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

                                        if (role.equals("customer") && isResetPassTruEmail == 1){
                                            String hashedPassword = PassUtils.hashPassword(password);

                                            user.updatePassword(hashedPassword)
                                                .addOnCompleteListener(task1 -> {
                                                   if (task1.isSuccessful()){
                                                       // Update 'isResetPassTruEmail' field to 0 after login
                                                       db.collection("users").document(user.getUid())
                                                           .update(
                                                                   "isResetPassTruEmail", 0,
                                                                   "password", hashedPassword
                                                           )
                                                           .addOnSuccessListener(aVoid -> {
                                                               // Successfully updated 'isResetPassTruEmail'
                                                               context.startActivity(intent);

                                                               // Finish the activity if context is an Activity
                                                               if (context instanceof Activity) {
                                                                   ((Activity) context).finish();
                                                               }
                                                           })
                                                           .addOnFailureListener(e -> {
                                                               ShowToast.showDelayedToast(context, progressBar, loginText, "Failed to update reset status.", timeDelayInMillis);
                                                           });
                                                   }
                                                });
                                        } else {
                                            context.startActivity(intent);

                                            // Finish the activity if context is an Activity
                                            if (context instanceof Activity) {
                                                ((Activity) context).finish();
                                            }
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
