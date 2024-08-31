package UserHomePageDirectory;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Handler.StatusBarUtil;
import UserHomePageDirectory.AddressList.AddressSelection;

public class OrderConfirmation extends AppCompatActivity {
    private OrdersAdapter ordersAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button proceed_btn;
    private ImageView back_btn;
    private TextInputEditText edt_user_address;
    private TextInputEditText messageInput;
    private TextView edt_item_total_price;
    private MaterialCardView gCash_btn, cashOnDelivery;
    private boolean isGcashSelected = false;
    private boolean isDateSelected = false;  // Track if date is selected
    private TextView selectedDate;
    private long minDateInMillis;
    private long maxDateInMillis;
    private MaterialCardView btnPickDate;

    // selected address
    private TextView txt_Full_name, txt_mobile_number, txt_order_address;
    private Map<String, Object> currentDefaultAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);

        // get the items added from orders
        Intent intent = getIntent();
        AddedItems addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        Log.d("CartManager", "orderConfirmation");
        addedItems.logCartItems();

        // display the added items to the recycler view
        RecyclerView recyclerView = findViewById(R.id.rv_order_confirm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemsList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(itemsList, this);
        recyclerView.setAdapter(ordersAdapter);

        // initialize variables
        gCash_btn = findViewById(R.id.G_cash_button);
        cashOnDelivery = findViewById(R.id.Cash_on_delivery_button);
        btnPickDate = findViewById(R.id.btn_pick_date);
        back_btn = findViewById(R.id.btn_back);
        proceed_btn = findViewById(R.id.btn_proceed);

        selectedDate = findViewById(R.id.selected_date);
        edt_item_total_price = findViewById(R.id.itemTotalPrice);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // update and get the current address
        LinearLayout linearLayoutButton = findViewById(R.id.address_selector_button);
        updateDeliveryAddress();

        showCurrentOrders(addedItems);

        // Calculate the date range
        Calendar today = Calendar.getInstance();
        minDateInMillis = today.getTimeInMillis();

        // Add 5 days to today's date
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_YEAR, 5);
        maxDateInMillis = maxDate.getTimeInMillis();
        maxDate.set(Calendar.HOUR_OF_DAY, 23);
        maxDate.set(Calendar.MINUTE, 59);
        maxDate.set(Calendar.SECOND, 59);
        maxDate.set(Calendar.MILLISECOND, 999);

        maxDateInMillis = maxDate.getTimeInMillis();

        // Set up button click listener
        btnPickDate.setOnClickListener(view -> showDatePickerDialog());

        // display item total price
        String itemTotalPriceFmt = "₱" + addedItems.getTotalAmount();
        edt_item_total_price.setText(itemTotalPriceFmt);

        // Set the click listeners for the MaterialCardView buttons
        gCash_btn.setOnClickListener(view -> {
            togglePaymentMethod(gCash_btn, cashOnDelivery);
        });

        cashOnDelivery.setOnClickListener(view -> {
            togglePaymentMethod(cashOnDelivery, gCash_btn);
        });

        back_btn.setOnClickListener(view -> {
            Intent backIntent = new Intent(OrderConfirmation.this, MainDashboardUser.class);
            startActivity(backIntent);
            finish();
        });

        // Set up proceed button
        proceed_btn.setOnClickListener(view -> {
            if (!currentDefaultAddress.isEmpty()) {
                if (!isDateSelected) {
                    Toast.makeText(this, "Please select a delivery date.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (gCash_btn.isChecked() || cashOnDelivery.isChecked()) {
                    // get the additional message
                    messageInput = findViewById(R.id.message_input);
                    String additionalMessage = String.valueOf(messageInput.getText());

                    if (isGcashSelected) {
                        // Navigate to the Gcash confirmation screen
                        Intent proceedIntent = new Intent(OrderConfirmation.this,GcashConfirmation.class);
                        proceedIntent.putExtra("addedItems", addedItems);
                        proceedIntent.putExtra("deliveryAddress", (Serializable) currentDefaultAddress);
                        proceedIntent.putExtra("additionalMessage", additionalMessage);
                        startActivity(proceedIntent);
                    } else {
                        // Navigate to the Cash on Delivery confirmation screen
                        Intent proceedIntent = new Intent(OrderConfirmation.this, OrderReceipt.class);
                        proceedIntent.putExtra("addedItems", addedItems);
                        proceedIntent.putExtra("deliveryAddress", (Serializable) currentDefaultAddress);
                        proceedIntent.putExtra("additionalMessage", additionalMessage);
                        proceedIntent.putExtra("modeOfPayment", "Cash on Delivery");
                        startActivity(proceedIntent);
                    }
                    finish();
                } else {
                    Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(OrderConfirmation.this, "Enter your delivery address.", Toast.LENGTH_SHORT).show();
            }
        });

        linearLayoutButton.setOnClickListener(view -> {
            Intent backintent = new Intent(OrderConfirmation.this, AddressSelection.class);
            backintent.putExtra("addedItems", addedItems);
            backintent.putExtra("fromOrderConfirmation", true);
            startActivity(backintent);
        });
    }

    private void togglePaymentMethod(MaterialCardView selectedCard, MaterialCardView otherCard) {
        selectedCard.setChecked(!selectedCard.isChecked());

        if (selectedCard.isChecked()) {
            otherCard.setChecked(false);

            // Update the payment method based on the selected card
            isGcashSelected = selectedCard == gCash_btn;
        }
    }

    private void showCurrentOrders(AddedItems addedItems) {
        List<Map<String, Object>> cartItems = addedItems.getCartItems();
        for (Map<String, Object> itemMap : cartItems) {
            GetItems item = mapToGetItems(itemMap);
            itemsList.add(item);
        }
        ordersAdapter.notifyDataSetChanged();
    }

    private GetItems mapToGetItems(Map<String, Object> map) {
        String itemId = (String) map.get("item_id");
        String itemImg = (String) map.get("item_img");
        String itemName = (String) map.get("item_name");
        Integer itemPrice = (Integer) map.get("item_price");
        Integer itemOrderQuantity = (Integer) map.get("item_order_quantity");
        Integer itemTotalPrice = (Integer) map.get("item_total_price");

        return new GetItems(itemName, itemPrice, itemImg, itemOrderQuantity, itemTotalPrice);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = getDatePickerDialog(year, month, day);

        // Set the minimum and maximum date for the DatePickerDialog
        datePickerDialog.getDatePicker().setMinDate(minDateInMillis);
        datePickerDialog.getDatePicker().setMaxDate(maxDateInMillis);

        // Show the date picker dialog
        datePickerDialog.show();
    }

    private @NonNull DatePickerDialog getDatePickerDialog(int year, int month, int day) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDatePickerDialog, this::onDateSet, year, month, day);
        datePickerDialog.setOnShowListener(dialog -> {
            Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
            Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);

            // Change text color and background programmatically
            positiveButton.setTextColor(getResources().getColor(R.color.button_bg));
            negativeButton.setTextColor(getResources().getColor(R.color.button_bg));

        });
        return datePickerDialog;
    }

    @SuppressLint("SetTextI18n")
    private void onDateSet(DatePicker view, int year1, int month1, int dayOfMonth) {
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year1, month1, dayOfMonth);
        long selectedDateInMillis = selectedCalendar.getTimeInMillis();

        if (selectedDateInMillis >= minDateInMillis && selectedDateInMillis <= maxDateInMillis) {
            // Format and display the selected date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(selectedCalendar.getTime());
            selectedDate.setText(String.format("Selected Date: %s", formattedDate));
            isDateSelected = true;  // Set date selected flag to true
        } else {
            selectedDate.setText("Selected date is out of range");
            isDateSelected = false;  // Reset date selected flag
        }
    }


    // this method will update the default address displayed on the order confirmation screen
    // flow:
    // 1. initialize the variables respected to their ids
    // 2. get the current user login then locate the user data to the firebase
    // 3. iterate through the 'deliveryDetails' field then look for the default address
    // 4. if located then update the details then set that object for saving
    // 5. else display error message
    private void updateDeliveryAddress() {
        LinearLayout addressLayout = findViewById(R.id.address_selector_button);

        txt_Full_name = addressLayout.findViewById(R.id.Full_name);
        txt_mobile_number = addressLayout.findViewById(R.id.mobile_number);
        txt_order_address = addressLayout.findViewById(R.id.order_address);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<Map<String, Object>> deliveryDetailsList = (List<Map<String, Object>>) documentSnapshot.get("deliveryDetails");

                            if (deliveryDetailsList != null) {
                                // Loop through the list to find the default address
                                for (Map<String, Object> details : deliveryDetailsList) {
                                    int isDefaultAddress = ((Long) details.get("isDefaultAddress")).intValue();

                                    if (isDefaultAddress == 1) {
                                        String fullName = (String) details.get("fullName");
                                        String mobileNumber = (String) details.get("phoneNumber");
                                        String orderAddress = (String) details.get("deliveryAddress");

                                        // Update the UI with the default address
                                        txt_Full_name.setText(fullName);
                                        txt_mobile_number.setText(mobileNumber);
                                        txt_order_address.setText(orderAddress);
                                        currentDefaultAddress = details;
                                        break;
                                    }
                                }
                            }
                        } else {
                            // Handle the case where the user data doesn't exist
                            Log.d("OrderConfirmation", "User data does not exist");
                            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that occur while retrieving the data
                        Log.e("OrderConfirmation", "Error fetching user data", e);
                        Toast.makeText(this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle the case where the user is not authenticated
            Log.d("OrderConfirmation", "User not authenticated");
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }
}
