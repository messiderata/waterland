package LoginDirectory;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

import HelpFul.ShowToast;

public class HandleLogin {

    private final Context context;
    private final ProgressBar progressBar;
    private final TextView loginText;
    private final int timeDelayInMillis;

    // Constructor to initialize the necessary fields
    public HandleLogin(Context context, ProgressBar progressBar, TextView loginText, int timeDelayInMillis) {
        this.context = context;
        this.progressBar = progressBar;
        this.loginText = loginText;
        this.timeDelayInMillis = timeDelayInMillis;
    }

    // Method to handle login failures
    public void handleLoginFailure(Exception exception) {
        try {
            throw Objects.requireNonNull(exception);
        } catch (FirebaseAuthInvalidUserException e) {
            ShowToast.showDelayedToast(context, progressBar, loginText, "This account does not exist.", timeDelayInMillis);
        } catch (FirebaseAuthInvalidCredentialsException e) {
            ShowToast.showDelayedToast(context, progressBar, loginText, "Incorrect password.", timeDelayInMillis);
        } catch (Exception e) {
            ShowToast.showDelayedToast(context, progressBar, loginText, "Login failed: " + e.getMessage(), timeDelayInMillis);
        }
    }
}
