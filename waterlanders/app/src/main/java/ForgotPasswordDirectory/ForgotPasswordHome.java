package ForgotPasswordDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.waterlanders.R;

public class ForgotPasswordHome extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_home);

        CardView phoneButton = findViewById(R.id.phoneButton);
        CardView emailButton = findViewById(R.id.emailButton);
        ImageView backIcon = findViewById(R.id.backIcon);

        phoneButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(ForgotPasswordHome.this, ForgotPasswordPhone.class);
            startActivity(backIntent);
        });
        emailButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(ForgotPasswordHome.this, ForgotPasswordEmail.class);
            startActivity(backIntent);
        });
        backIcon.setOnClickListener(view -> {
            finish();
        });

    }
}