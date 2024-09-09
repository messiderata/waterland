package UserHomePageDirectory.FragmentsDirectory;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.waterlanders.R;
import com.google.firebase.firestore.DocumentSnapshot;


public class ProfileFragment extends Fragment {

    TextView username;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize your CardView buttons here
        username = view.findViewById(R.id.user_username);

        // Retrieve and display user data
        FireStoreHelper firestoreHelper = new FireStoreHelper();
        firestoreHelper.getUserData(new FireStoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                displayCurrentUser(document);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle error case here, like showing a Toast or a Snackbar
                // e.g., Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private void displayCurrentUser(DocumentSnapshot document) {
        String userUserName = document.getString("username");

        username.setText(userUserName);
    }
}