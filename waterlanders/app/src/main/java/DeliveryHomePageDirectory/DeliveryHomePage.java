package DeliveryHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;

import AdminHomePageDirectory.AdminHomePage;
import LoginDirectory.Login;

public class DeliveryHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_home_page);

        Button logout_button = findViewById(R.id.button);

        logout_button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(DeliveryHomePage.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeliveryHomePage.this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}