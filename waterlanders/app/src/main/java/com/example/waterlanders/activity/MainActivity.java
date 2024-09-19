package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler; // Import Handler class
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;
import LoginDirectory.Login;
import UserHomePageDirectory.MainDashboardUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        performFirebaseQuery();
    }

    private void performFirebaseQuery() {
        // Check if the user is already logged in
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        runOnUiThread(() -> {
            if (firebaseUser != null) {
                // If the user is logged in, redirect them based on their role
                redirectUser(firebaseUser);
            } else {
                // If the user is not logged in, show the login screen
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void redirectUser(FirebaseUser firebaseUser) {
        db.collection("users").document(firebaseUser.getUid()).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    String role = document.getString("role");
                    Intent intent;
                    if (role != null) {
                        switch (role) {
                            case "ADMIN":
                                intent = new Intent(this, AdminHomePage.class);
                                break;
                            case "DELIVERY":
                                intent = new Intent(this, DeliveryHomePage.class);
                                break;
                            case "customer":
                                intent = new Intent(this, MainDashboardUser.class);
                                break;
                            default:
                                Toast.makeText(this, "LOGIN SUCCESSFUL. Unknown role.", Toast.LENGTH_SHORT).show();
                                return;
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "LOGIN SUCCESSFUL. Role is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "LOGIN SUCCESSFUL. Failed to retrieve role.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
