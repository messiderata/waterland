package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.waterlanders.R;

import LoginDirectory.Login;

public class registration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        ImageView backIcon = findViewById(R.id.backIcon);
        EditText FullName2 = findViewById(R.id.FullName2);
        EditText username2 = findViewById(R.id.username2);
        EditText add2 = findViewById(R.id.add2);
        EditText Num2 = findViewById(R.id.Num2);
        CardView registerbtn = findViewById(R.id.registerbtn);

        registerbtn.setOnClickListener(view -> {
            Intent backIntent = new Intent(registration.this, success.class);
            backIntent.putExtra("success_message","Account Registered");
            backIntent.putExtra("success_description","The account is succesfully registered");
            startActivity(backIntent);
        });

        backIcon.setOnClickListener(view -> {
            finish();
        });

    }
}