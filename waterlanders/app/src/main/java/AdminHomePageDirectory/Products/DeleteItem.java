package AdminHomePageDirectory.Products;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import AdminHomePageDirectory.SuccessScreen;

public class DeleteItem extends AppCompatActivity {

    private ImageView backButton;

    private ImageView productItemImg;
    private TextView itemID;
    private TextView itemName;
    private TextView itemPrice;

    private Button backButton2;
    private Button deleteButton;

    private FirebaseFirestore db;
    private ItemsConstructor currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_delete_item);
        initializeObjects();
        getIntentsData();

        // event listeners for buttons
        backButton.setOnClickListener(v -> {
            finish();
        });

        backButton2.setOnClickListener(v -> {
            finish();
        });

        deleteButton.setOnClickListener(v -> {
            deleteItemOnFirestore();
        });
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);

        productItemImg = findViewById(R.id.product_item_img);
        itemID = findViewById(R.id.item_id);
        itemName = findViewById(R.id.item_name);
        itemPrice = findViewById(R.id.item_price);

        backButton2 = findViewById(R.id.back_button_2);
        deleteButton = findViewById(R.id.delete_button);

        db = FirebaseFirestore.getInstance();
    }

    private void getIntentsData(){
        Intent intent = getIntent();
        currentItem = (ItemsConstructor) intent.getSerializableExtra("current_item");

        // set the text base on the item clicked

        // display the icon
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(currentItem.getItem_img());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image using Glide with the download URL
            Glide.with(this).load(uri).into(productItemImg);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        });

        itemID.setText(String.valueOf(currentItem.getItem_id()));
        itemName.setText(String.valueOf(currentItem.getItem_name()));
        itemPrice.setText(String.format("₱" + currentItem.getItem_price()));
    }

    private void deleteItemOnFirestore(){
        String itemId = currentItem.getItem_id();
        db.collection("items").document(itemId).delete()
            .addOnSuccessListener(aVoid -> {
                Intent showSucessScreenIntent = new Intent(this, SuccessScreen.class);
                showSucessScreenIntent.putExtra("success_message", "ITEM DELETED\n" + "SUCCESSFULLY");
                showSucessScreenIntent.putExtra("success_description", "The item has been removed from the database");
                showSucessScreenIntent.putExtra("fragment", "products");
                startActivity(showSucessScreenIntent);
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e("DELETE ITEM", "ERROR: " + e);
                Toast.makeText(this, "Failed to delete item from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}