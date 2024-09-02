package UserHomePageDirectory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GcashConfirmation extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private TextView uploadText;
    private TextInputEditText referenceNumber;
    private String referenceNumberInput;
    private CardView submitButton;
    private Uri imageUri;

    // passed intent values
    private AddedItems addedItems;
    private Map<String, Object> currentDefaultAddress;
    private String additionalMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcash_confirmation);

        uploadText = findViewById(R.id.upload_text);
        submitButton = findViewById(R.id.submit_button);

        // receive the data pass from the OrderConfirmation.java
        Intent receiveIntent = getIntent();
        addedItems = (AddedItems) receiveIntent.getSerializableExtra("addedItems");
        currentDefaultAddress = (Map<String, Object>) receiveIntent.getSerializableExtra("deliveryAddress");
        additionalMessage = (String) receiveIntent.getSerializableExtra("additionalMessage");

        // handle image upload if the 'browse your image' layout is clicked
        findViewById(R.id.upload_image).setOnClickListener(v -> handleImageSelection());

        // onclick event listener to handle the uploaded image
        // and save the image to the firebase storage /order_receipts
        submitButton.setOnClickListener(v -> {
            if (imageUri != null) {
                // check first if customer input the reference number
                // because since customer choose to may via gcash
                // then there must always be reference number
                referenceNumber = findViewById(R.id.reference_number);
                referenceNumberInput = String.valueOf(referenceNumber.getText());

                if (!referenceNumberInput.isEmpty()){
                    uploadImageToFirebase(imageUri);
                } else {
                    Toast.makeText(this, "Please input reference number first.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleImageSelection() {
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

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // i comment this line because we want to prevent multiple file upload in the firebase storage
        // so it only takes 1 image receipt per order
        // intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_CODE_PICK_IMAGE);
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

    private void uploadImageToFirebase(Uri uri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("order_receipts");
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String storageLocation = fileRef.toString();

                        // Create a Map to store image URL and reference number
                        Map<String, Object> GcashPaymentDetailsMap = new HashMap<>();
                        GcashPaymentDetailsMap.put("imageLink", storageLocation);
                        GcashPaymentDetailsMap.put("referenceNumber", referenceNumberInput);

                        // pass the value to the OrderReceipt.java to save to the firestore
                        Intent orderReceiptIntent = new Intent(this, OrderReceipt.class);
                        orderReceiptIntent.putExtra("addedItems", addedItems);
                        orderReceiptIntent.putExtra("deliveryAddress", (Serializable) currentDefaultAddress);
                        orderReceiptIntent.putExtra("additionalMessage", additionalMessage);
                        orderReceiptIntent.putExtra("modeOfPayment", "GCash");
                        orderReceiptIntent.putExtra("GCashPaymentDetails", (Serializable) GcashPaymentDetailsMap);
                        startActivity(orderReceiptIntent);
                        finish();

                    }).addOnFailureListener(e -> {
                        Toast.makeText(GcashConfirmation.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GcashConfirmation.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileExtension(Uri uri) {
        String extension = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            extension = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).split("\\.")[1];
            cursor.close();
        }
        return extension;
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
}