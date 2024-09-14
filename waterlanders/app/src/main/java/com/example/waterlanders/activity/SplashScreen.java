package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;


public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the splash screen layout
        setContentView(R.layout.activity_main);

        // Set a delay for showing the splash screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity after the delay
                Intent intent = new Intent(SplashScreen.this,MainActivity.class);startActivity(intent);
                // Finish the splash activity so it won't be visible on back press
                finish();
            }
        }, 2000); // 2-second delay
    }
}
