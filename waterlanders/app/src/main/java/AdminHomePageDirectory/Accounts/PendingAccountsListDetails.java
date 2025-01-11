package AdminHomePageDirectory.Accounts;

import android.app.Activity;
import android.app.Dialog;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;
import java.util.Objects;

import AdminHomePageDirectory.Chats.ChatUsersConstructor;

public class PendingAccountsListDetails extends AppCompatActivity {

    private ImageView backButton;
    private TextView customerName;
    private TextView customerID;
    private TextView customerUserName;
    private TextView customerEmail;
    private TextView customerContact;
    private TextView customerAccountStatus;
    private TextView customerDeliveryAddress;
    private ImageView uploadedID;
    private Button rejectButton;
    private Button acceptButton;
    private ChatUsersConstructor chatUsersConstructor;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_pending_accounts_list_details);
        getIntentData();
        initializeObjects();
        setObjectValues();
        setOnclickListeners();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        chatUsersConstructor = (ChatUsersConstructor) intent.getSerializableExtra("current_user");
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);
        customerName = findViewById(R.id.customer_name);
        customerID = findViewById(R.id.customer_id);
        customerUserName = findViewById(R.id.account_username);
        customerEmail = findViewById(R.id.account_email);
        customerContact = findViewById(R.id.customer_contact_number);
        customerAccountStatus = findViewById(R.id.account_status);
        customerDeliveryAddress = findViewById(R.id.customer_delivery_address);
        uploadedID = findViewById(R.id.uploaded_id);
        rejectButton = findViewById(R.id.reject_button);
        acceptButton = findViewById(R.id.approve_button);
        db = FirebaseFirestore.getInstance();
    }

    private void setObjectValues(){
        customerName.setText(chatUsersConstructor.getFullName());
        customerID.setText(chatUsersConstructor.getUserID());
        customerUserName.setText(chatUsersConstructor.getUsername());
        customerEmail.setText(chatUsersConstructor.getEmail());
        customerAccountStatus.setText(chatUsersConstructor.getAccountStatus());

        // display image
        String upload_url = chatUsersConstructor.getUploadedID();
        if (upload_url != null){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(upload_url);
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // Load image using Glide with the download URL
                Glide.with(this).load(uri).into(uploadedID);
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            });
        }


        // display main address
        for (Map<String, Object> details : chatUsersConstructor.getDeliveryDetails()) {
            int isDefaultAddress = ((Long) details.get("isDefaultAddress")).intValue();

            if (isDefaultAddress == 1) {
                String mobileNumber = (String) details.get("phoneNumber");
                String orderAddress = (String) details.get("deliveryAddress");

                customerContact.setText(mobileNumber);
                customerDeliveryAddress.setText(orderAddress);
                break;
            }
        }
    }

    private void setOnclickListeners(){
        backButton.setOnClickListener(view -> finish());

        rejectButton.setOnClickListener(view -> {
            showDialogContent("Confirm Reject Account", "Are you sure you want to reject this account?", chatUsersConstructor.getUserID(), 2);
        });

        acceptButton.setOnClickListener(view -> {
            showDialogContent("Confirm Approve Account", "Are you sure you want to approve this account?", chatUsersConstructor.getUserID(), 1);
        });
    }

    private void showDialogContent(String title, String description, String userID, int mode){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_account_status);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);

        TextView DialogTitle = dialog.findViewById(R.id.dialog_title);
        TextView DialogDescription = dialog.findViewById(R.id.dialog_description);
        MaterialButton btnCancel = dialog.findViewById(R.id.button_cancel);
        MaterialButton btnApprove = dialog.findViewById(R.id.button_approve);

        DialogTitle.setText(title);
        DialogDescription.setText(description);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApprove.setOnClickListener(v -> {
            if (mode == 1){
                db.collection("users")
                        .document(userID)
                        .update("accountStatus", "APPROVED")
                        .addOnSuccessListener(aVoid -> {
                            Log.d("PendingAccountsAdapter", "Account status updated to APPROVED.");
                            dialog.dismiss();
                            Intent intent = new Intent(this, PendingAccountsList.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Log.d("PendingAccountsAdapter", "Failed to update account status: " + e.getMessage()));
            } else {
                db.collection("users")
                        .document(userID)
                        .update("accountStatus", "REJECTED")
                        .addOnSuccessListener(aVoid -> {
                            Log.d("PendingAccountsAdapter", "Account status updated to REJECTED.");
                            dialog.dismiss();

                            // for all orders on 'user_id' field same as userID in 'pendingOrders' collection
                            // transfer them all in 'cancelledOrders' collection then delete in from 'pendingOrders' collection
                            // Query the 'pendingOrders' collection for orders with 'user_id' equal to userID
                            db.collection("pendingOrders")
                                    .whereEqualTo("user_id", userID)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        // Loop through all documents retrieved
                                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                            // Add each document to 'cancelledOrders' collection
                                            db.collection("cancelledOrders")
                                                    .document(document.getId())
                                                    .set(document.getData())
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Log.d("PendingAccountsAdapter", "Order transferred to cancelledOrders.");

                                                        // Delete the document from 'pendingOrders'
                                                        db.collection("pendingOrders")
                                                                .document(document.getId())
                                                                .delete()
                                                                .addOnSuccessListener(aVoid2 -> Log.d("PendingAccountsAdapter", "Order deleted from pendingOrders."))
                                                                .addOnFailureListener(e -> Log.d("PendingAccountsAdapter", "Failed to delete order from pendingOrders: " + e.getMessage()));
                                                    })
                                                    .addOnFailureListener(e -> Log.d("PendingAccountsAdapter", "Failed to transfer order to cancelledOrders: " + e.getMessage()));
                                        }

                                        Intent intent = new Intent(this, PendingAccountsList.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Log.d("PendingAccountsAdapter", "Failed to retrieve pending orders: " + e.getMessage()));
                        })
                        .addOnFailureListener(e -> Log.d("PendingAccountsAdapter", "Failed to update account status: " + e.getMessage()));
            }

        });

        dialog.show();
    }
}