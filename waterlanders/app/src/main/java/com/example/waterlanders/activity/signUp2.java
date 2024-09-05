package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class signUp2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);

        EditText emailField = findViewById(R.id.emailField);
        EditText passField = findViewById(R.id.passField);
        EditText confirmField = findViewById(R.id.confirmField);
        CardView registerbtn = findViewById(R.id.registerbtn);
        TextView login_account = findViewById(R.id.login_account);

        registerbtn.setOnClickListener(view -> {
            Intent backIntent = new Intent(signUp2.this, registration.class);
            startActivity(backIntent);
        });

        login_account.setOnClickListener(view -> {
            finish();
        });
    }
}