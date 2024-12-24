package AdminHomePageDirectory.AdminFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;

import AdminHomePageDirectory.Accounts.AccountStatusList;
import AdminHomePageDirectory.Accounts.PendingAccountsList;
import LoginDirectory.Login;

public class AccountFragment extends Fragment {

    private Button logoutButton;
    private LinearLayout pendingAccounts;
    private LinearLayout accountStatus;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_account, container, false);

        pendingAccounts = view.findViewById(R.id.pending_accounts);
        pendingAccounts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PendingAccountsList.class);
            startActivity(intent);
        });

        accountStatus = view.findViewById(R.id.account_status);
        accountStatus.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AccountStatusList.class);
            startActivity(intent);
        });

        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }
}