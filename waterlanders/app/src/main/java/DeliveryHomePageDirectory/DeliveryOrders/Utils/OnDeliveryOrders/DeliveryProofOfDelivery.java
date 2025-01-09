package DeliveryHomePageDirectory.DeliveryOrders.Utils.OnDeliveryOrders;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import AdminHomePageDirectory.Orders.Utils.OnDeliveryOrders.OnDeliveryOrdersConstructor;
import DeliveryHomePageDirectory.DeliveryOrders.Utils.DeliverySuccessScreen;
import UserHomePageDirectory.MainDashboardUser;

public class DeliveryProofOfDelivery extends AppCompatActivity {

    private ImageView backButton;

    private LinearLayout uploadImageContainer;
    private TextView uploadText;

    private Button backButton2;
    private Button saveButton;

    private OnDeliveryOrdersConstructor onDeliveryOrdersConstructor;

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseDatabase rdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_proof_of_delivery);
        initializeObjects();
        getIntentData();

        // other buttons
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
                showPaymentDialog();
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);

        uploadImageContainer = findViewById(R.id.upload_image_container);
        uploadText = findViewById(R.id.upload_text);

        backButton2 = findViewById(R.id.back_button_2);
        saveButton = findViewById(R.id.save_button);

        db = FirebaseFirestore.getInstance();
        rdb = FirebaseDatabase.getInstance();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        onDeliveryOrdersConstructor = intent.getParcelableExtra("current_order");
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

    private void uploadImageToFirebase(Uri uri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("proof_of_delivery");
        String formattedItemName = String.valueOf(uploadText.getText()).replace(" ", "-");
        StorageReference fileRef = storageReference.child(formattedItemName + ".png");

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String storageLocation = fileRef.toString();
                        updateOrder(storageLocation);

                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateOrder(String storageLocation){
        String orderId = onDeliveryOrdersConstructor.getOrder_id();

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("accountStatus", onDeliveryOrdersConstructor.getAccountStatus());
        orderData.put("additional_message", onDeliveryOrdersConstructor.getAdditional_message());
        orderData.put("customer_feedback", "");
        orderData.put("date_delivered", Timestamp.now());
        orderData.put("date_delivery", onDeliveryOrdersConstructor.getDate_delivery());
        orderData.put("date_ordered", onDeliveryOrdersConstructor.getDate_ordered());
        orderData.put("delivery_address", onDeliveryOrdersConstructor.getDelivery_address());
        orderData.put("delivery_id", onDeliveryOrdersConstructor.getDelivery_id());
        orderData.put("isPaid", onDeliveryOrdersConstructor.getIsPaid());
        orderData.put("mode_of_payment", onDeliveryOrdersConstructor.getMode_of_payment());
        orderData.put("order_icon", onDeliveryOrdersConstructor.getOrder_icon());
        orderData.put("order_id", onDeliveryOrdersConstructor.getOrder_id());
        orderData.put("order_items", onDeliveryOrdersConstructor.getOrder_items());
        orderData.put("order_status", "DELIVERED");
        orderData.put("proof_of_delivery", storageLocation);
        orderData.put("search_term", onDeliveryOrdersConstructor.getSearch_term());
        orderData.put("total_amount", onDeliveryOrdersConstructor.getTotal_amount());
        orderData.put("user_confirmation", "PENDING");
        orderData.put("user_id", onDeliveryOrdersConstructor.getUser_id());

        if (String.valueOf(onDeliveryOrdersConstructor.getMode_of_payment()).equals("GCash")){
            orderData.put("gcash_payment_details", onDeliveryOrdersConstructor.getGcash_payment_details());
        }

        db.collection("onDelivery").document(orderId).delete()
            .addOnSuccessListener(aVoid -> {
                db.collection("deliveredOrders").document(orderId).set(orderData)
                    .addOnSuccessListener(aVoid1 -> {
                        updateOrderStatusInRealtimeDatabase("DELIVERED");

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to move order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete order from onDelivery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateOrderStatusInRealtimeDatabase(String orderStatus){
        String orderId = onDeliveryOrdersConstructor.getOrder_id();
        String userId = onDeliveryOrdersConstructor.getUser_id();
        DatabaseReference myRef = rdb.getReference(userId).child("orders").child(orderId);

        // Create a map with the updated status
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("orderStatus", orderStatus);

        // Update the order data
        myRef.updateChildren(updateData)
            .addOnSuccessListener(aVoid -> {
                // Order updated successfully
                Intent showSucessScreenIntent = new Intent(this, DeliverySuccessScreen.class);
                showSucessScreenIntent.putExtra("success_message", "ORDER UPDATED\n" + "SUCCESSFULLY");
                showSucessScreenIntent.putExtra("success_description", "order has been moved to your \n" + "‘DELIVERED’ tab");
                showSucessScreenIntent.putExtra("fragment", "orders");
                startActivity(showSucessScreenIntent);
                finish();
            })
            .addOnFailureListener(e -> {
                // Failed to update order
                Log.e("Order", "Failed to update order", e);
            });
    }

    private void showPaymentDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_delivered_order_payment);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);

        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextView dialogDescription = dialog.findViewById(R.id.dialog_description);
        MaterialButton btnCancel = dialog.findViewById(R.id.button_cancel);
        MaterialButton btnNo = dialog.findViewById(R.id.button_no);
        MaterialButton btnYes = dialog.findViewById(R.id.button_yes);

        dialogTitle.setText("Payment Confirmation");
        dialogDescription.setText("Was the payment transaction successful?");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            db.collection("onDelivery")
                    .document(onDeliveryOrdersConstructor.getOrder_id())
                    .update("isPaid", "YES")
                    .addOnSuccessListener(aVoid -> {
                        onDeliveryOrdersConstructor.setIsPaid("YES");
                        uploadImageToFirebase(imageUri);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "An error occurred updating order.", Toast.LENGTH_SHORT).show();
                        Log.d("delete pending order", e.toString());
                    });
        });

        btnNo.setOnClickListener(v -> {
            db.collection("onDelivery")
                    .document(onDeliveryOrdersConstructor.getOrder_id())
                    .update("isPaid", "NO")
                    .addOnSuccessListener(aVoid -> {
                        onDeliveryOrdersConstructor.setIsPaid("NO");
                        uploadImageToFirebase(imageUri);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "An error occurred updating order.", Toast.LENGTH_SHORT).show();
                        Log.d("delete pending order", e.toString());
                    });
        });

        dialog.show();
    }
}