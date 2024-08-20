package UserHomePageDirectory;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import Handler.StatusBarUtil;
import LoginDirectory.Login;
import UserHomePageDirectory.FragmentsDirectory.AboutUsFragment;
import UserHomePageDirectory.FragmentsDirectory.HelpUsFragment;
import UserHomePageDirectory.FragmentsDirectory.HistoryFragment;
import UserHomePageDirectory.FragmentsDirectory.HomeFragment;
import UserHomePageDirectory.FragmentsDirectory.SettingsFragment;

public class MainDashboardUser extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private TextView editText;
    private static final String TitleTextHome = "Home";
    private static final String TitleTextSettings = "Settings";
    private static final String TITLE_TEXT_AboutUs = "About Us";
    private static final String TitleTextHelp = "Help";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dasboard_user);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);


        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        editText = findViewById(R.id.title_text_top);
        NavigationView navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        ImageView menuItem = findViewById(R.id.menu_icon);


        // Initialize and configure dialog


        // Set default item checked in NavigationView
        Menu menu = navigationView.getMenu();
        MenuItem itemChecked = menu.findItem(R.id.home_nav);
        itemChecked.setChecked(true);

        // Set up drawer toggle
        menuItem.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set initial fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // Set up NavigationView item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.home_nav) {
                selectedFragment = new HomeFragment();
                editText.setText(TitleTextHome);
                bottomNavigationView.setVisibility(View.VISIBLE);
            } else if (itemId == R.id.settings_nav) {
                selectedFragment = new SettingsFragment();
                editText.setText(TitleTextSettings);
                bottomNavigationView.setVisibility(View.GONE);
            } else if (itemId == R.id.about_nav) {
                selectedFragment = new AboutUsFragment();
                editText.setText(TITLE_TEXT_AboutUs);
                bottomNavigationView.setVisibility(View.GONE);
            } else if (itemId == R.id.help_nav) {
                selectedFragment = new HelpUsFragment();
                editText.setText(TitleTextHelp);
                bottomNavigationView.setVisibility(View.GONE);
            }else if (itemId == R.id.logout_nav) {
                showLogoutDialog();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set up BottomNavigationView item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_order) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new HistoryFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Handle back press with BackPressHandler
        new BackPressHandler(this, drawerLayout);
    }

    // Call this method to show the dialog when needed
    private void showLogoutDialog() {
        // Initialize and configure dialog
        Dialog dialog = new Dialog(MainDashboardUser.this);
        dialog.setContentView(R.layout.logout_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);

        // Find views in the dialog
        MaterialButton btnCancel = dialog.findViewById(R.id.button_cancel);
        MaterialButton btnOk = dialog.findViewById(R.id.button_ok);

        // Set click listeners for buttons
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            // Perform logout action
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainDashboardUser.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Show the dialog
        dialog.show();
    }
}


