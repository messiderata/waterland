package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.waterlanders.R;

import LoginDirectory.Login;

public class OTP extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        ImageView backIcon = findViewById(R.id.backIcon);
        Button confirm = findViewById(R.id.confirm);

        confirm.setOnClickListener(view -> {
            Intent backIntent = new Intent(OTP.this, newPass.class);
            startActivity(backIntent);
        });

        backIcon.setOnClickListener(view -> {
            finish();
        });

    }
}