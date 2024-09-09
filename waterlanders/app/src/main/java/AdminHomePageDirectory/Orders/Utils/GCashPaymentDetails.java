package AdminHomePageDirectory.Orders.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.waterlanders.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class GCashPaymentDetails extends AppCompatActivity {

    private ImageView backButton;
    private ImageView gCashIMG;
    private TextView referenceNumber;
    private Button backButton2;

    private Map<String, Object> gCashPaymentDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcash_payment_details);
        initializeObject();
        getIntentValues();
        setObjectValues();

        // event listeners for the back buttons
        backButton.setOnClickListener(v -> {
            finish();
        });

        backButton2.setOnClickListener(v -> {
            finish();
        });
    }

    private void initializeObject(){
        backButton = findViewById(R.id.back_button);
        gCashIMG = findViewById(R.id.gcash_img);
        referenceNumber = findViewById(R.id.reference_number);
        backButton2 = findViewById(R.id.back_button_2);
    }

    private void getIntentValues(){
        Intent intent = getIntent();
        gCashPaymentDetails = (Map<String, Object>) intent.getSerializableExtra("gcash_payment_details");
    }

    private void setObjectValues(){
        // display image
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(String.valueOf(gCashPaymentDetails.get("imageLink")));
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image using Glide with the download URL
            Glide.with(this).load(uri).into(gCashIMG);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        });

        referenceNumber.setText(String.valueOf(gCashPaymentDetails.get("referenceNumber")));
    }
}