package com.example.waterlanders.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

public class PhonePass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_pass);

        ImageView backIcon = findViewById(R.id.backIcon);
        ImageView lockIcon = findViewById(R.id.lockIcon);
        TextView ForgotPass = findViewById(R.id.ForgotPass);
        TextView subTitleText = findViewById(R.id.subtitleText);
        EditText phoneField = findViewById(R.id.phoneField);
        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(PhonePass.this, OTP.class);
            startActivity(backIntent);
        });

        backIcon.setOnClickListener(view -> {
            finish();
        });

    }

}