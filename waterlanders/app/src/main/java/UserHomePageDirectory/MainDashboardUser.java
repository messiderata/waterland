package UserHomePageDirectory;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.waterlanders.NotificationService;
import com.example.waterlanders.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import Handler.StatusBarUtil;
import LoginDirectory.Login;
import UserHomePageDirectory.FragmentsDirectory.AboutUsFragment;
import UserHomePageDirectory.FragmentsDirectory.ChatActivity;
import UserHomePageDirectory.FragmentsDirectory.HelpUsFragment;
import UserHomePageDirectory.FragmentsDirectory.HistoryFragment;
import UserHomePageDirectory.FragmentsDirectory.HomeFragment;
import UserHomePageDirectory.FragmentsDirectory.ProfileFragment;
import UserHomePageDirectory.FragmentsDirectory.SettingsFragment;

public class MainDashboardUser extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private TextView editText;
    private static final String TitleTextHome = "Dashboard";
    private static final String TitleTextSettings = "Settings";
    private static final String TITLE_TEXT_AboutUs = "About Us";
    private static final String titleTextHelp = "Help";
    private static final  String titleMeText = "Me";

    private static final int SMS_PERMISSION_REQUEST_CODE = 1;

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

        getPermission();
        initializeNotificationService();

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        editText = findViewById(R.id.title_text_top);
        NavigationView navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        ImageView menuItem = findViewById(R.id.menu_icon);


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

        initializeFragment(savedInstanceState);

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
                editText.setText(titleTextHelp);
                bottomNavigationView.setVisibility(View.GONE);
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
                editText.setText(TitleTextHome);

            } else if (itemId == R.id.nav_history) {
                editText.setText(TitleTextHome);
                selectedFragment = new HistoryFragment();
            } else if (itemId == R.id.nav_chat) {
                Intent chatIntent = new Intent(this, ChatActivity.class);
                chatIntent.putExtra("receiver_id", "NVWcwGTdD1VdMcnUg86IBAUuE3i2");
                startActivity(chatIntent);
            } else if (itemId == R.id.nav_profile) {
                editText.setText(TitleTextHome);
                selectedFragment = new ProfileFragment();
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

    private void initializeNotificationService(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent serviceIntent = new Intent(this, NotificationService.class);
        serviceIntent.putExtra("userId", userId);

        // Check if the service is already running
        if (!isServiceRunning(NotificationService.class)) {
            startService(serviceIntent);
        } else {
            Log.d("Service Status", "NotificationService is already running.");
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void initializeFragment(Bundle savedInstanceState){
        Intent intent = getIntent();
        String fragmentToOpen = intent.getStringExtra("open_fragment");

        if (fragmentToOpen != null && fragmentToOpen.equals("history")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HistoryFragment())
                    .commit();
        } else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    private void getPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS Permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "SMS Permission denied. The app will not be able to send OTP.", Toast.LENGTH_LONG).show();
            }
        }
    }
}


