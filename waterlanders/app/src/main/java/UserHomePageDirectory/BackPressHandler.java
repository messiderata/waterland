package UserHomePageDirectory;

import android.os.Handler;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class BackPressHandler extends OnBackPressedCallback {

    private final DrawerLayout drawerLayout;
    private boolean doubleBackToExitPressedOnce = false;
    private final AppCompatActivity activity;

    public BackPressHandler(AppCompatActivity activity, DrawerLayout drawerLayout) {
        super(true); // Enabled by default
        this.drawerLayout = drawerLayout;
        this.activity = activity;

        // Handle back press
        activity.getOnBackPressedDispatcher().addCallback(activity, this);
    }

    @Override
    public void handleOnBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                activity.finishAffinity(); // Close all activities and exit the app
            } else {
                doubleBackToExitPressedOnce = true;
                Toast.makeText(activity, "Press back again to exit", Toast.LENGTH_SHORT).show();

                // Reset the flag after 2 seconds
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        }
    }
}
