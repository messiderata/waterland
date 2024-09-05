package com.example.waterlanders.activity;

import android.os.Bundle;
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

public class newPass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pass);

        ImageView backIcon = findViewById(R.id.backIcon);
        EditText newP = findViewById(R.id.newP);
        EditText conP = findViewById(R.id.conP);
        Button confirm = findViewById(R.id.confirm);

        backIcon.setOnClickListener(view -> {
            finish();
        });
    }
}