package UserHomePageDirectory.OrderTrackingFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.waterlanders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import AdminHomePageDirectory.Orders.Fragments.AdminCancelledOrdersFragment;
import AdminHomePageDirectory.Orders.Fragments.AdminPaidOrdersFragment;

public class UserMoreOrderMenuFragment extends Fragment {

    private BottomNavigationView topNavigationView;

    public UserMoreOrderMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_more_order_menu, container, false);
        initializeObjects(view);
        initializeFirstFragment(savedInstanceState);

        // Set up BottomNavigationView item selection
        // this handles the fragment state which fragment is active
        topNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.paid) {
                selectedFragment = new UserPaidOrdersFragment();
            } else if (itemId == R.id.cancelled) {
                selectedFragment = new UserCancelledOrdersFragment();
            } else if (itemId == R.id.delivered) {
                selectedFragment = new UserCompletedOrdersFragment();
            }
            if (selectedFragment != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.user_orders_fragment2_container, selectedFragment)
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
                    .replace(R.id.user_orders_fragment2_container, new UserCompletedOrdersFragment())
                    .commit();
        }
    }
}