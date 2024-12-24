package AdminHomePageDirectory.Accounts;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import AdminHomePageDirectory.Chats.ChatUsersAdapter;
import AdminHomePageDirectory.Chats.ChatUsersConstructor;

public class AccountStatusList extends AppCompatActivity {

    private ImageView backButton;
    private RecyclerView accountsListContainer;
    private List<ChatUsersConstructor> chatUsersConstructors;
    private AccountStatusListAdapter accountStatusListAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_account_status_list);
        initializeObjects();
        setOnClickListeners();
        populateAccountList();
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);
        accountsListContainer = findViewById(R.id.accounts_list_container);
        accountsListContainer.setLayoutManager(new LinearLayoutManager(this));

        chatUsersConstructors = new ArrayList<>();
        accountStatusListAdapter = new AccountStatusListAdapter(this, chatUsersConstructors);
        accountsListContainer.setAdapter(accountStatusListAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setOnClickListeners(){
        backButton.setOnClickListener(view -> finish());
    }

    private void populateAccountList(){
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> userList = task.getResult().getDocuments();

                for (DocumentSnapshot document : userList){
                    String currentUserID = auth.getCurrentUser().getUid();
                    if (!document.getId().equals(currentUserID)) {
                        ChatUsersConstructor chatUsers = document.toObject(ChatUsersConstructor.class);
                        if (chatUsers != null){
                            chatUsers.setUserID(document.getId());
                            chatUsersConstructors.add(chatUsers);
                            Log.d("AccountStatusList", "Document Data: " + document.getData());
                        }
                    }
                }

                Log.d("AccountStatusList", "chatUsersConstructors size: " + chatUsersConstructors.size());
                for (ChatUsersConstructor user : chatUsersConstructors) {
                    Log.d("AccountStatusList", "User: " + user.getFullName()); // Replace with relevant field
                }
                Log.d("AccountStatusList", "RecyclerView visibility: " + accountsListContainer.getVisibility());

                accountStatusListAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}