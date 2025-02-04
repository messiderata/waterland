package SignUpDirectory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.waterlanders.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Handler.PassUtils;

public class UserSignUpAdditionalInfo extends AppCompatActivity {

    private ImageView backIcon;
    private TextInputEditText fullName;
    private TextInputEditText userName;
    private AutoCompleteTextView addressInput;
    private AutoCompleteTextView postalInput;
    private TextInputEditText streetAddress;
    private TextInputEditText contactNumber;
    private CardView registerButton;
    private LinearLayout uploadImageContainer;
    private Uri imageUri;
    private TextView uploadText;

    private String userEmail;
    private String userPassword;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final String ITEM_ADDRESS = "NCR, Metro Manila, Marikina, Barangka";
    private static final String ITEM_POSTAL = "1803";
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up_additional_info);
        initializeObject();
        getIntentData();
        setFixAddress();

        uploadImageContainer.setOnClickListener(view -> handleImageUpload());

        registerButton.setOnClickListener(view -> checkInputDetails());

        backIcon.setOnClickListener(view -> finish());

    }

    private void initializeObject(){
        backIcon = findViewById(R.id.back_icon);
        fullName = findViewById(R.id.full_name);
        userName = findViewById(R.id.user_name);
        addressInput = findViewById(R.id.address_input);
        postalInput = findViewById(R.id.postal_input);
        streetAddress = findViewById(R.id.street_address);
        contactNumber = findViewById(R.id.contact_number);
        registerButton = findViewById(R.id.register_button);
        uploadImageContainer = findViewById(R.id.upload_image_container);
        uploadText = findViewById(R.id.upload_text);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // set filter for contact number
        contactNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    private void getIntentData(){
        Intent intent = getIntent();
        userEmail = (String) intent.getSerializableExtra("user_email");
        userPassword = (String) intent.getSerializableExtra("user_pass");
    }

    private void setFixAddress(){
        ArrayAdapter<String> addressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{ITEM_ADDRESS});
        addressInput.setAdapter(addressAdapter);

        ArrayAdapter<String> postalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{ITEM_POSTAL});
        postalInput.setAdapter(postalAdapter);
    }

    private void checkInputDetails(){
        boolean isComplete = true;
        if (TextUtils.isEmpty(fullName.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Full Name first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(userName.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your User Name first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(addressInput.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Address first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(postalInput.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Postal first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(streetAddress.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Street first.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(contactNumber.getText())){
            isComplete = false;
            Toast.makeText(this, "Input your Contact Number first.", Toast.LENGTH_SHORT).show();
        } else if (!String.valueOf(contactNumber.getText()).matches("^\\d{10}$")) {
            isComplete = false;
            Toast.makeText(this, "Phone number must be exactly 10 digits. +63 is already given.", Toast.LENGTH_SHORT).show();
        } else if (imageUri == null){
            isComplete = false;
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
        }

        if (isComplete) {
            isContactNumberUnique(contactNumber.getText().toString(), unique -> {
                if (unique) {
                    isUsernameUnique(userName.getText().toString());
                } else {
                    Toast.makeText(this, "Phone number already registered.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void isContactNumberUnique(String phoneNumber, OnContactNumberChecked callback) {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> usersList = task.getResult().getDocuments();
                boolean isUnique = true;

                for (DocumentSnapshot user : usersList) {
                    String userRole = (String) user.get("role");
                    if (userRole != null && userRole.equals("customer")) {
                        List<Map<String, Object>> deliveryDetailsList = (List<Map<String, Object>>) user.get("deliveryDetails");
                        for (Map<String, Object> details : deliveryDetailsList) {
                            String existingPhoneNumber = String.valueOf(details.get("phoneNumber"));
                            if (phoneNumber.equals(existingPhoneNumber)) {
                                isUnique = false;
                                break;
                            }
                        }
                    }
                    if (!isUnique) break;
                }
                callback.onResult(isUnique);
            } else {
                Toast.makeText(this, "Failed to retrieve users data", Toast.LENGTH_SHORT).show();
                callback.onResult(false);
            }
        });
    }

    interface OnContactNumberChecked {
        void onResult(boolean isUnique);
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

    private void isUsernameUnique(String username){
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // Username already exists
                    Toast.makeText(this, "Username already registered.", Toast.LENGTH_SHORT).show();

                } else {
                    // Email and Username are unique, proceed to create user
                    // createUser();
                    uploadImageToFirebase();
                }
            });
    }

    private void uploadImageToFirebase() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("accountIDs");
        String formattedItemName = String.valueOf(uploadText.getText()).replace(" ", "-");
        StorageReference fileRef = storageReference.child(formattedItemName + ".png");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String storageLocation = fileRef.toString();
                        createUser(storageLocation);

                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createUser(String uploadedImgID){
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("email", userEmail);
        newUser.put("fullName", String.valueOf(fullName.getText()));
        newUser.put("username", String.valueOf(userName.getText()));
        newUser.put("role", "customer");
        newUser.put("isResetPassTruEmail", 0);
        newUser.put("accountStatus", "PENDING");
        newUser.put("verificationStatus", "PENDING");
        newUser.put("uploadedID", uploadedImgID);
        newUser.put("unreadMessagesFromAdminToUser", 0);
        newUser.put("unreadMessagesFromUserToAdmin", 0);
        newUser.put("unreadMessagesDate", "");

        List<Map<String, Object>> deliveryDetailsList = new ArrayList<>();

        Map<String, Object> deliveryDetails = new HashMap<>();
        String formatedAddress = String.format(streetAddress.getText() + ", " + addressInput.getText() + ", " + postalInput.getText());
        deliveryDetails.put("deliveryAddress", formatedAddress);
        deliveryDetails.put("fullName", String.valueOf(fullName.getText()));
        deliveryDetails.put("isDefaultAddress", 1);

        String formattedPhoneNumber = String.format("+63"+contactNumber.getText());
        deliveryDetails.put("phoneNumber", formattedPhoneNumber);
        deliveryDetailsList.add(deliveryDetails);

        newUser.put("deliveryDetails", deliveryDetailsList);

        String hashedPassword = PassUtils.hashPassword(userPassword);
        newUser.put("password", hashedPassword);

        mAuth.createUserWithEmailAndPassword(userEmail, hashedPassword)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Get the user ID of the newly created user
                    String userId = Objects.requireNonNull(task.getResult().getUser()).getUid();

                    // Set the document ID to be the user ID
                    db.collection("users").document(userId)
                        .set(newUser)
                        .addOnSuccessListener(aVoid -> {
                            verifyUserEmail();
                            Intent successIntent = new Intent(UserSignUpAdditionalInfo.this, UserSignUpSuccess.class);
                            successIntent.putExtra("success_message","Account Registered");
                            successIntent.putExtra("success_description","The account is successfully registered");
                            startActivity(successIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "ERROR CREATING AN ACCOUNT.\n" + e,
                                Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "ERROR CREATING AN ACCOUNT.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void verifyUserEmail(){
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent to " + userEmail, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }
}