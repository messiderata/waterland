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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;

public class GcashConfirmation extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private TextView uploadText;
    private TextInputEditText referenceNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcash_confirmation);

        uploadText = findViewById(R.id.upload_text);
        referenceNumber = findViewById(R.id.reference_number);

        // receive the data pass from the OrderConfirmation.java
        Intent receiveIntent = getIntent();
        AddedItems addedItems = (AddedItems) receiveIntent.getSerializableExtra("addedItems");
        Map<String, Object> currentDefaultAddress = (Map<String, Object>) receiveIntent.getSerializableExtra("deliveryAddress");
        String additionalMessage = (String) receiveIntent.getSerializableExtra("additionalMessage");

        findViewById(R.id.upload_image).setOnClickListener(v -> handleImageSelection());
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
            if (data != null) {
                // Handle single image selection
                if (data.getData() != null) {
                    Uri uri = data.getData();
                    String fileName = getFileName(uri);
                    uploadText.setText(fileName);
                }
                // Handle multiple images selection
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    StringBuilder fileNames = new StringBuilder();
                    for (int i = 0; i < count; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        fileNames.append(getFileName(uri)).append("\n");
                    }
                    uploadText.setText(fileNames.toString());
                }
            }
        }
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