package com.example.waterlanders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class forgot_password extends AppCompatActivity {

    private Button btn_send, btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_send = findViewById(R.id.fgt_send_code);
        btn_back = findViewById(R.id.fgt_back);

        // do something
        btn_send.setOnClickListener(view -> {
            Intent intent = new Intent(forgot_password.this, forgot_password.class);
            startActivity(intent);
            finish();
        });

        // redirect to login
        btn_back.setOnClickListener(view -> {
            Intent intent = new Intent(forgot_password.this, login.class);
            startActivity(intent);
            finish();
        });
    }
}