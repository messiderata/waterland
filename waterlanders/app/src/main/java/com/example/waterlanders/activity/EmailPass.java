package com.example.waterlanders.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.waterlanders.R;

public class EmailPass extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_pass);

        ImageView backIcon = findViewById(R.id.backIcon);
        ImageView lockIcon = findViewById(R.id.lockIcon);
        TextView ForgotPass = findViewById(R.id.ForgotPass);
        TextView subTitleText = findViewById(R.id.subtitleText);
        EditText emailField = findViewById(R.id.emailField);
        Button sendButton = findViewById(R.id.sendButton);

        backIcon.setOnClickListener(view -> {
            finish();
        });

    }
}
