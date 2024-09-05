package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.waterlanders.R;

import UserHomePageDirectory.MainDashboardUser;
import UserHomePageDirectory.OrderConfirmation;

public class ForgotPassword extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        CardView phoneButton = findViewById(R.id.phoneButton);
        CardView emailButton = findViewById(R.id.emailButton);
        TextView subtitleText = findViewById(R.id.subtitleText);
        ImageView lockIcon = findViewById(R.id.lockIcon);
        ImageView backIcon = findViewById(R.id.backIcon);
        TextView makeSelectionText = findViewById(R.id.makeSelectionText);

        phoneButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(ForgotPassword.this, PhonePass.class);
            startActivity(backIntent);
        });
        emailButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(ForgotPassword.this, EmailPass.class);
            startActivity(backIntent);
        });
        backIcon.setOnClickListener(view -> {
            finish();
        });

    }
}