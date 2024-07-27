package UserHomePageDirectory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.waterlanders.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import LoginDirectory.Login;
import UserHomePageDirectory.FragmentsDirectory.AboutUsFragment;
import UserHomePageDirectory.FragmentsDirectory.HomeFragment;
import UserHomePageDirectory.FragmentsDirectory.SettingsFragment;

public class MainDashboardUser extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private boolean doubleBackToExitPressedOnce = false;
    private TextView editText;
    private ImageView menuItem;

    String titleTextHome = "Home";
    String titleTextSettings = "Settings";
    String titleTextAboutUs = "About Us";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dasboard_user); // Ensure this is the correct layout resource

        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // User is not logged in, redirect to login activity
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish(); // Prevent going back to this activity
            return; // Exit onCreate to prevent further initialization
        }

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        editText = findViewById(R.id.title_text_top);
        menuItem = findViewById(R.id.menu_icon);
        CardView logoutButton  = findViewById(R.id.logout_nav);

        // Initialize NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Disable drawer sliding
        disableDrawerSliding();

        menuItem.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Load the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(this, Login.class);
            startActivity(logoutIntent);
            finish();
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home_nav) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
            editText.setText(titleTextHome);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.settings_nav) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment()).commit();
            editText.setText(titleTextSettings);
            drawerLayout.closeDrawer(GravityCompat.START);

        } else if (id == R.id.about_nav) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AboutUsFragment()).commit();
            editText.setText(titleTextAboutUs);
            drawerLayout.closeDrawer(GravityCompat.START);
        }  else {
            Toast.makeText(this, "Unknown option selected", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                finishAffinity(); // Close all activities and exit the app
            } else {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

                // Reset the flag after 2 seconds
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        }
    }

    private void disableDrawerSliding() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
}
