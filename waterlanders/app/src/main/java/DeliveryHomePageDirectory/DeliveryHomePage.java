package DeliveryHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.waterlanders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import DeliveryHomePageDirectory.DeliveryFragments.DeliveryAccountFragment;
import DeliveryHomePageDirectory.DeliveryFragments.DeliveryDashboardFragment;
import DeliveryHomePageDirectory.DeliveryFragments.DeliveryOrdersFragment;
import LoginDirectory.Login;

public class DeliveryHomePage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView titleTextTop;

    // titles
    private final String DASHBOARD_TITLE = "DASHBOARD";
    private final String ORDERS_TITLE = "ORDERS";
    private final String ACCOUNT_TITLE = "ACCOUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_home_page);
        checkCurrentUserIfLogIn();
        checkIntentFragment(savedInstanceState);
        initializeObjects();

        // Set up BottomNavigationView item selection
        // this handles the fragment state which fragment is active
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.dashboard) {
                selectedFragment = new DeliveryDashboardFragment();
                titleTextTop.setText(DASHBOARD_TITLE);
            } else if (itemId == R.id.orders) {
                selectedFragment = new DeliveryOrdersFragment();
                titleTextTop.setText(ORDERS_TITLE);
            } else if (itemId == R.id.account) {
                selectedFragment = new DeliveryAccountFragment();
                titleTextTop.setText(ACCOUNT_TITLE);
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    private void checkCurrentUserIfLogIn(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkIntentFragment(Bundle savedInstanceState){
        Intent intent = getIntent();
        String fragmentToOpen = intent.getStringExtra("open_fragment");

        if (fragmentToOpen != null && fragmentToOpen.equals("orders")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DeliveryOrdersFragment())
                    .commit();
        } else {
            initializeFirstFragment(savedInstanceState);
        }
    }

    private void initializeFirstFragment(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DeliveryDashboardFragment())
                    .commit();
        }
    }

    private void initializeObjects(){
        titleTextTop = findViewById(R.id.title_text_top);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

}
