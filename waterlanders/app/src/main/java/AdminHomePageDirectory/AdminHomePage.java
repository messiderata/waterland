package AdminHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;

import LoginDirectory.Login;

public class AdminHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home_page);

        Button logout_button = findViewById(R.id.button);

        logout_button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(AdminHomePage.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminHomePage.this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}