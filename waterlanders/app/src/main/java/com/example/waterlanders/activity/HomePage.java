package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomePage extends AppCompatActivity {

    TextView txt_h_email, txt_h_fullName, txt_h_username, txt_h_pass, txt_h_address, txt_h_role;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // init obj
        Button logout_button = findViewById(R.id.logout_btn);
        txt_h_email = findViewById(R.id.home_email);
        txt_h_fullName = findViewById(R.id.home_fullName);
        txt_h_username = findViewById(R.id.home_username);
        txt_h_pass = findViewById(R.id.home_password);
        txt_h_address = findViewById(R.id.home_address);
        txt_h_role = findViewById(R.id.home_role);
        db = FirebaseFirestore.getInstance();

        // get current user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Log.d("HomePage", "userId: " + userId);
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Retrieve and display user details
                        displayCurrentUser(document);
                    } else {
                        Toast.makeText(HomePage.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomePage.this, "Failed to fetch document", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(HomePage.this, "User not logged in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePage.this, Login.class);
            startActivity(intent);
            finish();
        }

        logout_button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(HomePage.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePage.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void displayCurrentUser(DocumentSnapshot document) {
        String email = document.getString("email");
        String fullName = document.getString("fullName");
        String username = document.getString("username");
        String password = document.getString("password");
        String address = document.getString("address");
        String role = document.getString("role");

        txt_h_email.setText(email);
        txt_h_fullName.setText(fullName);
        txt_h_username.setText(username);
        txt_h_pass.setText(password);
        txt_h_address.setText(address);
        txt_h_role.setText(role);
    }
}
