package ForgotPasswordDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;

import LoginDirectory.Login;

public class ForgotPasswordSuccess extends AppCompatActivity {

    private TextView successMessage;
    private TextView successDescription;
    private Button continueButton;

    private String successMessageIntent;
    private String successDescriptionIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_success);
        initializeObjects();
        getIntents();
        setMessageInputs();

        continueButton.setOnClickListener(v -> {
            Intent backToLoginIntent = new Intent(this, Login.class);
            startActivity(backToLoginIntent);
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