package AdminHomePageDirectory.AdminFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.waterlanders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import AdminHomePageDirectory.Orders.Fragments.AdminDeliveredFragment;
import AdminHomePageDirectory.Orders.Fragments.AdminOnDeliveryFragment;
import AdminHomePageDirectory.Orders.Fragments.AdminPendingOrdersFragment;
import AdminHomePageDirectory.Orders.Fragments.AdminWaitingForCourierFragment;

public class OrdersFragment extends Fragment {

    private BottomNavigationView topNavigationView;

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);
        initializeObjects(view);
        initializeFirstFragment(savedInstanceState);

        // Set up BottomNavigationView item selection
        // this handles the fragment state which fragment is active
        topNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.pending_orders) {
                selectedFragment = new AdminPendingOrdersFragment();
            } else if (itemId == R.id.waiting_for_courier) {
                selectedFragment = new AdminWaitingForCourierFragment();
            } else if (itemId == R.id.on_delivery) {
                selectedFragment = new AdminOnDeliveryFragment();
            } else if (itemId == R.id.delivered) {
                selectedFragment = new AdminDeliveredFragment();
            }
            if (selectedFragment != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.admin_orders_fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        return view;
    }

    private void initializeObjects(View view){
        topNavigationView = view.findViewById(R.id.top_navigation);
    }

    private void initializeFirstFragment(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.admin_orders_fragment_container, new AdminPendingOrdersFragment())
                    .commit();
        }
    }
}