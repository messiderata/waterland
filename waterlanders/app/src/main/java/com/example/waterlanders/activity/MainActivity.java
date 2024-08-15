package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Import Handler class

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;

import LoginDirectory.Login;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);


        // Declare Handler variable
        Handler handler = new Handler(); // Initialize Handler

        // Post delayed execution
        handler.postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }, 1); // Changed delay to 3000 milliseconds// Corrected delayMillis to 3000 (milliseconds)

    }
}
