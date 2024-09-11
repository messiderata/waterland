package AdminHomePageDirectory.Products;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.waterlanders.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import AdminHomePageDirectory.SuccessScreen;

public class AddNewItem extends AppCompatActivity {

    private ImageView backButton;

    private EditText itemName;
    private EditText itemPrice;
    private LinearLayout uploadImageContainer;
    private TextView uploadText;

    private Button backButton2;
    private Button saveButton;

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private Uri imageUri;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_new_item);
        initializeObjects();

        // event listeners for buttons
        backButton.setOnClickListener(v -> {
            finish();
        });

        uploadImageContainer.setOnClickListener(v -> {
            handleImageUpload();
        });

        backButton2.setOnClickListener(v -> {
            finish();
        });

        saveButton.setOnClickListener(v -> {
            if (imageUri != null) {
                String itemNameInput = String.valueOf(itemName.getText());
                String itemPriceString = String.valueOf(itemPrice.getText());

                if (!itemNameInput.isEmpty() && !itemPriceString.isEmpty()) {
                    try {
                        int itemPriceInput = Integer.parseInt(itemPriceString);
                        uploadImageToFirebase(imageUri, itemNameInput, itemPriceInput);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid price input", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please input item name and item price first", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);

        itemName = findViewById(R.id.item_name);
        itemPrice = findViewById(R.id.item_price);
        uploadImageContainer = findViewById(R.id.upload_image_container);
        uploadText = findViewById(R.id.upload_text);

        backButton2 = findViewById(R.id.back_button_2);
        saveButton = findViewById(R.id.save_button);

        db = FirebaseFirestore.getInstance();
    }

    private void handleImageUpload(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Request READ_MEDIA_IMAGES permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openPhotoPicker();
            }
        } else {
            // Android 6.0+ - Request READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openPhotoPicker();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                openPhotoPicker();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to read your images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                String fileName = getFileName(imageUri);
                uploadText.setText(fileName);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase(Uri uri, String itemNameInput, int itemPriceInput) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("items");
        String formattedItemName = itemNameInput.replace(" ", "-");
        StorageReference fileRef = storageReference.child(formattedItemName + ".png");

        fileRef.putFile(uri)
            .addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String storageLocation = fileRef.toString();
                    saveItemToFirebase(storageLocation, itemNameInput, itemPriceInput);

                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void saveItemToFirebase(String storageLocation, String itemNameInput, int itemPriceInput){
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("item_img", storageLocation);
        newItem.put("item_name", itemNameInput);
        newItem.put("item_price", itemPriceInput);

        db.collection("items")
                .document()
                .set(newItem)
                .addOnSuccessListener(aVoid -> {
                    // new intent to show the animation
                    Intent showSucessScreenIntent = new Intent(this, SuccessScreen.class);
                    showSucessScreenIntent.putExtra("success_message", "ITEM ADDED\n" + "SUCCESSFULLY");
                    showSucessScreenIntent.putExtra("success_description", "new item is added to the database");
                    showSucessScreenIntent.putExtra("fragment", "products");
                    startActivity(showSucessScreenIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("ADD NEW ITEM", "ERROR: "+ e);
                    Toast.makeText(this, "Failed to save new item", Toast.LENGTH_SHORT).show();
                });
    }
}