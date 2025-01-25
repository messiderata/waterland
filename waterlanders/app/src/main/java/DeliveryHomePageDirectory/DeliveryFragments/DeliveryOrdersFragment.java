package DeliveryHomePageDirectory.DeliveryFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.waterlanders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import DeliveryHomePageDirectory.DeliveryOrders.Fragments.DeliveryDeliveredOrdersFragment;
import DeliveryHomePageDirectory.DeliveryOrders.Fragments.DeliveryOnDeliveryOrdersFragment;
import DeliveryHomePageDirectory.DeliveryOrders.Fragments.DeliveryPickUpOrdersFragment;
import DeliveryHomePageDirectory.DeliveryOrders.Fragments.DeliveryWaitingOrdersFragment;

public class DeliveryOrdersFragment extends Fragment {

    private BottomNavigationView topNavigationView;

    public DeliveryOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_orders, container, false);
        initializeObjects(view);
        initializeFirstFragment(savedInstanceState);

        // Set up BottomNavigationView item selection
        // this handles the fragment state which fragment is active
        topNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.waiting_for_courier) {
                selectedFragment = new DeliveryWaitingOrdersFragment();
            } else if (itemId == R.id.on_delivery) {
                selectedFragment = new DeliveryOnDeliveryOrdersFragment();
            } else if (itemId == R.id.pick_up) {
                selectedFragment = new DeliveryPickUpOrdersFragment();
            } else if (itemId == R.id.delivered) {
                selectedFragment = new DeliveryDeliveredOrdersFragment();
            }
            if (selectedFragment != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.delivery_orders_fragment_container, selectedFragment)
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
                    .replace(R.id.delivery_orders_fragment_container, new DeliveryWaitingOrdersFragment())
                    .commit();
        }
    }
}