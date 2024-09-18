package UserHomePageDirectory.OrderTrackingUtils.Completed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import Handler.ShowToast;
import UserHomePageDirectory.MainDashboardUser;

public class UserCompletedOrdersEditReview extends AppCompatActivity {

    private ImageView backButton;
    private TextView orderStatus;
    private RatingBar ratingBar;
    private AutoCompleteTextView customerConfirmation;
    private EditText reviewText;
    private Button backButton2;
    private Button saveButton;

    private String orderStatusIntent;
    private String customerFeedback;
    private String orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_completed_orders_edit_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeObjects();
        getIntentData();
        setUpOrderStatusSuggestions();
        setOnClickListeners();
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);
        orderStatus = findViewById(R.id.order_status);
        ratingBar = findViewById(R.id.ratingBar);
        customerConfirmation = findViewById(R.id.customer_confirmation);
        reviewText = findViewById(R.id.review_text);
        backButton2 = findViewById(R.id.back_button_2);
        saveButton = findViewById(R.id.save_button);
    }

    private void getIntentData(){
        Intent feedbackDetails = getIntent();
        orderStatusIntent = (String) feedbackDetails.getSerializableExtra("order_status");
        orderID = (String) feedbackDetails.getSerializableExtra("order_id");
        customerFeedback = (String) feedbackDetails.getSerializableExtra("customer_feedback");

        // set the values
        orderStatus.setText(orderStatusIntent);
        String[] actualFeedback = customerFeedback.split(">");
        reviewText.setText(actualFeedback[1]);
    }

    private void setUpOrderStatusSuggestions() {
        String[] orderStatusSuggestions = {"Received", "Reviewing"};

        // Create an ArrayAdapter for the suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, orderStatusSuggestions
        );

        // Set the adapter to the MultiAutoCompleteTextView
        customerConfirmation.setAdapter(adapter);
    }

    private void setOnClickListeners(){
        backButton.setOnClickListener(view -> finish());
        backButton2.setOnClickListener(view -> finish());
        saveButton.setOnClickListener(view -> checkInputData());
    }

    private void checkInputData(){
        boolean isComplete = true;
        float ratingValue = ratingBar.getRating();
        String orderStatusContent = customerConfirmation.getText().toString();
        String reviewTextContent = reviewText.getText().toString();

        if (ratingValue == 0) {
            Toast.makeText(this, "Please rate your experience!", Toast.LENGTH_SHORT).show();
            isComplete = false;
        } else if (orderStatusContent.isEmpty()) {
            Toast.makeText(this, "Please select an order status!", Toast.LENGTH_SHORT).show();
            isComplete = false;
        } else if (!orderStatusContent.equals("Received")) {
            if (!orderStatusContent.equals("Reviewing")) {
                Toast.makeText(this, "Please select an order status!", Toast.LENGTH_SHORT).show();
                isComplete = false;
            }
        } else if (reviewTextContent.isEmpty()) {
            Toast.makeText(this, "Please provide a review!", Toast.LENGTH_SHORT).show();
            isComplete = false;
        }

        if (isComplete){
            String formattedFeedback = "Rating: " + ratingValue + "star\n" + "> " + reviewTextContent;
            updateUserFeedback(formattedFeedback, orderStatusContent);
        }
    }

    private void updateUserFeedback(String formattedFeedback, String orderStatusContent){
        // update the 'customer_feedback' and 'user_confirmation' fields
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("deliveredOrders").document(orderID)
                .update(
                        "customer_feedback", formattedFeedback,
                        "user_confirmation", orderStatusContent
                )
                .addOnSuccessListener(aVoid -> {
                    Intent gotoMainDashBoardIntent = new Intent(this, MainDashboardUser.class);
                    gotoMainDashBoardIntent.putExtra("open_fragment", "history");
                    startActivity(gotoMainDashBoardIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("WEQWEQWEQW", "Error: "+e.getMessage());
                    Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show();
                });
    }
}