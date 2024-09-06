package DeliveryHomePageDirectory.DeliveryOrders.Utils;

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

import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;

public class DeliverySuccessScreen extends AppCompatActivity {

    private TextView successMessage;
    private TextView successDescription;
    private Button continueButton;

    private String successMessageIntent;
    private String successDescriptionIntent;
    private String currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_success_screen);
        initializeObjects();
        getIntents();
        setMessageInputs();

        continueButton.setOnClickListener(v -> {
            Intent showUpdatedProductsIntent = new Intent(this, DeliveryHomePage.class);
            showUpdatedProductsIntent.putExtra("open_fragment", currentFragment);
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
        currentFragment = (String) getIntentValues.getSerializableExtra("fragment");
    }

    private void setMessageInputs(){
        successMessage.setText(String.valueOf(successMessageIntent));
        successDescription.setText(String.valueOf(successDescriptionIntent));
    }
}