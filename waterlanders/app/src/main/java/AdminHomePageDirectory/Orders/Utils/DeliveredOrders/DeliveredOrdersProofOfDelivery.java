package AdminHomePageDirectory.Orders.Utils.DeliveredOrders;

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

public class DeliveredOrdersProofOfDelivery extends AppCompatActivity {

    private ImageView backButton;
    private ImageView proofIMG;
    private Button backButton2;

    private String proofOfDeliveryLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_delivered_orders_proof_of_delivery);
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
        proofIMG = findViewById(R.id.proof_img);
        backButton2 = findViewById(R.id.back_button_2);
    }

    private void getIntentValues(){
        Intent intent = getIntent();
        proofOfDeliveryLink = (String) intent.getSerializableExtra("proof_of_delivery_link");
    }

    private void setObjectValues(){
        // display image
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(proofOfDeliveryLink);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image using Glide with the download URL
            Glide.with(this).load(uri).into(proofIMG);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        });
    }
}