package UserHomePageDirectory.HomeFragmentUtils.AddressList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Handler.StatusBarUtil;
import UserHomePageDirectory.HomeFragmentUtils.AddedItems;
import UserHomePageDirectory.HomeFragmentUtils.OrderConfirmation;

public class AddressSelection extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AddressAdapter contactAdapter;
    private String intentFrom;
    private List<DeliveryDetails> deliveryDetails;
    private AddedItems addedItems;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ImageView backImage;
    private CardView continueButton;
    private LinearLayout newAddressButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_selection);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);
        getIntentData();
        initializeObjects();
        getDeliveryDetails(deliveryDetails);
        setOnClickListeners();
    }

    private void initializeObjects(){
        backImage = findViewById(R.id.btn_back);
        continueButton = findViewById(R.id.continue_button);
        newAddressButton = findViewById(R.id.add_new_address_button);

        deliveryDetails = new ArrayList<>();
        recyclerView = findViewById(R.id.address_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new AddressAdapter(deliveryDetails, this, addedItems);
        recyclerView.setAdapter(contactAdapter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        addedItems.logCartItems();
        intentFrom = intent.getStringExtra("intentFrom");
    }

    private void getDeliveryDetails(List<DeliveryDetails> deliveryDetails){
        String userId = mAuth.getCurrentUser().getUid();

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extract the list of delivery details
                        List<Map<String, Object>> deliveryDetailsList = (List<Map<String, Object>>) documentSnapshot.get("deliveryDetails");

                        if (deliveryDetailsList != null) {
                            // Loop through the list to add the address to the list
                            for (Map<String, Object> details : deliveryDetailsList) {
                                String fullName = (String) details.get("fullName");
                                String phoneNumber = (String) details.get("phoneNumber");
                                String deliveryAddress = (String) details.get("deliveryAddress");
                                int isDefaultAddress = ((Long) details.get("isDefaultAddress")).intValue();

                                // Create a new DeliveryDetails object and add it to the list
                                DeliveryDetails deliveryDetail = new DeliveryDetails(fullName, phoneNumber, deliveryAddress, isDefaultAddress);
                                deliveryDetails.add(deliveryDetail);
                            }
                            // Notify the adapter that the data has changed
                            contactAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Handle the case where the user data doesn't exist
                        Log.d("AddressSelection", "User data does not exist");
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur while retrieving the data
                    Log.e("AddressSelection", "Error fetching user data", e);
                    Toast.makeText(this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                });
        } else {
            // Handle the case where the user is not authenticated
            Log.d("AddressSelection", "User not authenticated");
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setOnClickListeners(){
        backImage.setOnClickListener(view -> finish());

        continueButton.setOnClickListener(view -> {
            if (intentFrom.equals("OrderConfirmation")){
                Intent backIntent = new Intent(AddressSelection.this, OrderConfirmation.class);
                backIntent.putExtra("addedItems", addedItems);
                startActivity(backIntent);
                finish();
            } else {
                finish();
            }
        });

        newAddressButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(AddressSelection.this, AddressInput.class);
            backIntent.putExtra("addedItems", addedItems);
            startActivity(backIntent);
            finish();
        });
    }
}