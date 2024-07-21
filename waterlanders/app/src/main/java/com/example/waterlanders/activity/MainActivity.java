package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Import Handler class
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declare Handler variable
        Handler handler = new Handler(); // Initialize Handler

        // Post delayed execution
        handler.postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }, 3000); // Changed delay to 3000 milliseconds// Corrected delayMillis to 3000 (milliseconds)

    }
}
