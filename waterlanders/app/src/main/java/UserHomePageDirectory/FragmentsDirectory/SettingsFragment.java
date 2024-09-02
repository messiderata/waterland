package UserHomePageDirectory.FragmentsDirectory;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.waterlanders.R;

public class SettingsFragment extends Fragment {

    CardView profileBtn, nameBtn, userNameBtn, phoneBtn, emailBtn, changePassBtn, addressBtn;
    TextView nameText, userNameText, phoneText, emailText;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize your CardView buttons here
        profileBtn = view.findViewById(R.id.My_profile_button);
        nameBtn = view.findViewById(R.id.name_button);
        userNameBtn = view.findViewById(R.id.username_button);
        phoneBtn = view.findViewById(R.id.phone_button);
        emailBtn = view.findViewById(R.id.email_button);
        changePassBtn = view.findViewById(R.id.change_pass_button);
        addressBtn = view.findViewById(R.id.address_button);

        // Initialize your text views here
        nameText = view.findViewById(R.id.name_text);
        userNameText = view.findViewById(R.id.username_text);
        phoneText = view.findViewById(R.id.phone_text);
        emailText = view.findViewById(R.id.email_text);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            Log.d("HomePage", "userId: " + userId);
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve and display user details
                            displayCurrentUser(document);
                        }
                    }
                }
            });
        }

        return view;
    }

    private void displayCurrentUser(DocumentSnapshot document) {
        String email = document.getString("email");
        String fullName = document.getString("fullName");
        String username = document.getString("username");
        String phoneNumber = document.getString("phone"); // Assuming this field exists in your Firestore

        String maskedEmail = maskEmail(email);
        String maskedPhoneNumber = maskPhoneNumber(phoneNumber);

        nameText.setText(fullName);
        emailText.setText(maskedEmail);
        userNameText.setText(username);
        phoneText.setText(maskedPhoneNumber);
    }

    private String maskEmail(String email) {
        if (email != null && email.contains("@")) {
            int atIndex = email.indexOf("@");

            // Get the first character before the @ symbol
            String firstLetter = email.substring(0, 1);

            // Create a string of asterisks with the same length as the rest of the part before "@"
            String maskedPart = new String(new char[atIndex - 1]).replace('\0', '*');

            // Combine the first letter, masked part, and the domain
            return firstLetter + maskedPart + email.substring(atIndex);
        }
        return email;
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.length() > 2) {
            String firstTwo = phoneNumber.substring(0, 2);
            String maskedPart = phoneNumber.substring(2).replaceAll(".", "*");
            return firstTwo + maskedPart;
        }
        return phoneNumber;
    }
}
