package com.example.waterlanders.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.waterlanders.R;

import AdminHomePageDirectory.AdminFragments.ProductsFragment;
import LoginDirectory.Login;

public class success extends AppCompatActivity {

    private TextView successMessage;
    private TextView successDescription;
    private Button continueButton;

    private String successMessageIntent;
    private String successDescriptionIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_success);
        initializeObjects();
        getIntents();
        setMessageInputs();

        continueButton.setOnClickListener(v -> {
            Intent showUpdatedProductsIntent = new Intent(this, Login.class);

            startActivity(showUpdatedProductsIntent);
            finish();
        });
    }

    private void initializeObjects(){
        successMessage = findViewById(R.id.success_message);
        successDescription = findViewById(R.id.success_description);
        continueButton = findViewById(R.id.continue_button);
    }

    private void getIntents(){
        Intent getIntentValues = getIntent();
        successMessageIntent = (String) getIntentValues.getSerializableExtra("success_message");
        successDescriptionIntent = (String) getIntentValues.getSerializableExtra("success_description");
    }

    private void setMessageInputs(){
        successMessage.setText(String.valueOf(successMessageIntent));
        successDescription.setText(String.valueOf(successDescriptionIntent));
    }
}