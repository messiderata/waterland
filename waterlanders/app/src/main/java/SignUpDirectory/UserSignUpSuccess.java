package SignUpDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import AdminHomePageDirectory.AdminHomePage;
import DeliveryHomePageDirectory.DeliveryHomePage;
import Handler.ShowToast;
import LoginDirectory.Login;
import UserHomePageDirectory.MainDashboardUser;

public class UserSignUpSuccess extends AppCompatActivity {

    private TextView successMessage;
    private TextView successDescription;
    private Button continueButton;

    private String successMessageIntent;
    private String successDescriptionIntent;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_sign_up_success);
        initializeObjects();
        getIntents();
        setMessageInputs();

        continueButton.setOnClickListener(v -> {
            redirectCurrentUser();
        });
    }

    private void initializeObjects(){
        successMessage = findViewById(R.id.success_message);
        successDescription = findViewById(R.id.success_description);
        continueButton = findViewById(R.id.continue_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void getIntents(){
        Intent getIntentValues = getIntent();
        successMessageIntent = (String) getIntentValues.getSerializableExtra("success_message");
        successDescriptionIntent = (String) getIntentValues.getSerializableExtra("success_description");
    }

    private void setMessageInputs(){
        successMessage.setText(String.valueOf(successMessageIntent));
        successDescription.setText(String.valueOf(successDescriptionIntent));
    }

    private void redirectCurrentUser(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        db.collection("users").document(firebaseUser.getUid()).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    String role = document.getString("role");
                    Intent intent;
                    if (role != null) {
                        switch (role) {
                            case "ADMIN":
                                intent = new Intent(this, AdminHomePage.class);
                                break;
                            case "DELIVERY":
                                intent = new Intent(this, DeliveryHomePage.class);
                                break;
                            case "customer":
                                intent = new Intent(this, MainDashboardUser.class);
                                break;
                            default:
                                Toast.makeText(this, "LOGIN SUCCESSFUL. Unknown role.", Toast.LENGTH_SHORT).show();
                                return;
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "LOGIN SUCCESSFUL. Role is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "LOGIN SUCCESSFUL. Failed to retrieve role.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}