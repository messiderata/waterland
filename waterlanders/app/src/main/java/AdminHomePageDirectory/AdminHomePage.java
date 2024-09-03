package AdminHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.waterlanders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import AdminHomePageDirectory.AdminFragments.AccountFragment;
import AdminHomePageDirectory.AdminFragments.DashboardFragment;
import AdminHomePageDirectory.AdminFragments.OrdersFragment;
import AdminHomePageDirectory.AdminFragments.ProductsFragment;
import Handler.StatusBarUtil;
import LoginDirectory.Login;

public class AdminHomePage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView titleTextTop;

    // titles
    private final String DASHBOARD_TITLE = "DASHBOARD";
    private final String PRODUCTS_TITLE = "PRODUCTS";
    private final String ORDERS_TITLE = "ORDERS";
    private final String ACCOUNT_TITLE = "ACCOUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);
        checkCurrentUserIfLogIn();
        initializeFirstFragment(savedInstanceState);

        // initialize objects
        titleTextTop = findViewById(R.id.title_text_top);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up BottomNavigationView item selection
        // this handles the fragment state which fragment is active
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.dashboard) {
                selectedFragment = new DashboardFragment();
                titleTextTop.setText(DASHBOARD_TITLE);
            } else if (itemId == R.id.products) {
                selectedFragment = new ProductsFragment();
                titleTextTop.setText(PRODUCTS_TITLE);
            } else if (itemId == R.id.orders) {
                selectedFragment = new OrdersFragment();
                titleTextTop.setText(ORDERS_TITLE);
            } else if (itemId == R.id.account) {
                selectedFragment = new AccountFragment();
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

    private void initializeFirstFragment(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }


}
