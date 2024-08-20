package Handler;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

public class StatusBarUtil {

    /**
     * Sets the status bar color for the given activity.
     *
     * @param activity  The activity for which the status bar color should be set.
     * @param colorResId The resource ID of the color to be applied to the status bar.
     */
    @SuppressLint("ObsoleteSdkInt")
    public static void setStatusBarColor(Activity activity, @ColorRes int colorResId) {
        if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
            return;
        }
        activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, colorResId));
    }
}
