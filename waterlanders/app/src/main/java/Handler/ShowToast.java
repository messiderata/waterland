package Handler;

import android.widget.Toast;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShowToast {

    public static void showDelayedToast(final Context context, final ProgressBar progressBar, final TextView loginText, final String message,long timeDelayInMillis) {
        progressBar.setVisibility(View.VISIBLE);
        loginText.setVisibility(View.GONE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            loginText.setVisibility(View.VISIBLE);
        }, timeDelayInMillis);
    }

    public static void unshowProgressBar(final ProgressBar progressBar, final TextView loginText, long timeDelayInMillis) {
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.INVISIBLE);
            loginText.setVisibility(View.VISIBLE);
        }, timeDelayInMillis); // Delay for the specified time
    }
}
