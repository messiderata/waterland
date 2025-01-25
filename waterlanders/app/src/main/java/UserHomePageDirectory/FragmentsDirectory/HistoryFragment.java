package UserHomePageDirectory.FragmentsDirectory;

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
import UserHomePageDirectory.OrderTrackingFragments.UserCompletedOrdersFragment;
import UserHomePageDirectory.OrderTrackingFragments.UserConfirmOrdersFragment;
import UserHomePageDirectory.OrderTrackingFragments.UserDeliveryOrdersFragment;
import UserHomePageDirectory.OrderTrackingFragments.UserMoreOrderMenuFragment;
import UserHomePageDirectory.OrderTrackingFragments.UserPendingOrdersFragment;
import UserHomePageDirectory.OrderTrackingFragments.UserPickupOrdersFragment;


public class HistoryFragment extends Fragment {

    private BottomNavigationView topNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        initializeObjects(view);
        initializeFirstFragment(savedInstanceState);

        // Set up BottomNavigationView item selection
        // this handles the fragment state which fragment is active
        topNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.pending) {
                selectedFragment = new UserPendingOrdersFragment();
            } else if (itemId == R.id.confirm) {
                selectedFragment = new UserConfirmOrdersFragment();
            } else if (itemId == R.id.delivery) {
                selectedFragment = new UserDeliveryOrdersFragment();
            } else if (itemId == R.id.pickup) {
                selectedFragment = new UserPickupOrdersFragment();
            } else if (itemId == R.id.more) {
                selectedFragment = new UserMoreOrderMenuFragment();
            }
            if (selectedFragment != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.user_history_fragment_container, selectedFragment)
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
                    .replace(R.id.user_history_fragment_container, new UserPendingOrdersFragment())
                    .commit();
        }
    }
}