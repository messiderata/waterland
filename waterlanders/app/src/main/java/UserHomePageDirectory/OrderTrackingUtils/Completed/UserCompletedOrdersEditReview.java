package UserHomePageDirectory.OrderTrackingUtils.Completed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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
    private MultiAutoCompleteTextView customerConfirmation;
    private EditText reviewText;
    private Button backButton2;
    private Button saveButton;

    private String orderStatusIntent;
    private String customerFeedback;

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
        customerFeedback = (String) feedbackDetails.getSerializableExtra("customer_feedback");

        // set the values
        orderStatus.setText(orderStatusIntent);
        reviewText.setText(customerFeedback);
    }

    private void setUpOrderStatusSuggestions() {
        String[] orderStatusSuggestions = {"Received", "Reviewing"};

        // Create an ArrayAdapter for the suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, orderStatusSuggestions
        );

        // Set the adapter to the MultiAutoCompleteTextView
        customerConfirmation.setAdapter(adapter);

        // Set tokenizer to recognize individual words (separated by commas)
        customerConfirmation.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
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
        }

        if (orderStatusContent.isEmpty()) {
            Toast.makeText(this, "Please select an order status!", Toast.LENGTH_SHORT).show();
            isComplete = false;
        }

        if (reviewTextContent.isEmpty()) {
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
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
                    Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show();
                });
    }
}