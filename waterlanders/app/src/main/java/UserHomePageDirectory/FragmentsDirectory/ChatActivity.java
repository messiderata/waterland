package UserHomePageDirectory.FragmentsDirectory;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import UserHomePageDirectory.MainDashboardUser;

public class ChatActivity extends AppCompatActivity {

    private ImageView backButton;
    //private RecyclerView chatContainer;
    private EditText textMessage;
    private ImageButton sendButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserID;
    private String receiverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeObjects();
        getIntentData();
        setOnclickListeners();
        loadMessages();

        if (receiverID.equals("NVWcwGTdD1VdMcnUg86IBAUuE3i2")){
            showChatNotification();
        }
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);
        //chatContainer = findViewById(R.id.chat_container);
        textMessage = findViewById(R.id.text_message);
        sendButton = findViewById(R.id.send_button);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        receiverID = intent.getStringExtra("receiver_id");
    }

    private void setOnclickListeners(){
        backButton.setOnClickListener(v -> finish());
        sendButton.setOnClickListener(v -> {
            String messageText = textMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(currentUserID, receiverID, messageText);
                loadMessages();
            }
        });
    }

    private void sendMessage(String senderID, String receiverID, String messageText) {
        // Create a new message map
        Map<String, Object> message = new HashMap<>();
        message.put("text", messageText);
        message.put("sender", senderID);
        message.put("receiver", receiverID);

        final String modifiedSenderID = senderID.equals("NVWcwGTdD1VdMcnUg86IBAUuE3i2") ? receiverID : senderID;

        // Add the message to the user's chats array in Firestore
        db.collection("messages")
                .document(modifiedSenderID) // Sender's document
                .get() // First, check if the document exists
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, update the "chats" array
                        db.collection("messages")
                                .document(modifiedSenderID)
                                .update("chats", FieldValue.arrayUnion(message)) // Add message to the array
                                .addOnSuccessListener(aVoid -> {
                                    textMessage.setText(""); // Clear the input field
                                    Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Document does not exist, create it with the message
                        Map<String, Object> newMessageData = new HashMap<>();
                        newMessageData.put("chats", Arrays.asList(message)); // Add the first message to the chats array

                        db.collection("messages")
                                .document(modifiedSenderID)
                                .set(newMessageData) // Create the document and set the initial message
                                .addOnSuccessListener(aVoid -> {
                                    textMessage.setText(""); // Clear the input field
                                    Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Failed to check sender's document", Toast.LENGTH_SHORT).show();
                });

    }

    private void loadMessages() {
        final String modifiedSenderID = currentUserID.equals("NVWcwGTdD1VdMcnUg86IBAUuE3i2") ? receiverID : currentUserID;
        Log.d("ChatActivity", "modifiedSenderID: "+modifiedSenderID);
        Log.d("ChatActivity", "receiverID: "+receiverID);
        Log.d("ChatActivity", "currentUserID: "+currentUserID);
        db.collection("messages")
            .document(modifiedSenderID) // Load chats for the current user
            .addSnapshotListener((documentSnapshot, e) -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    List<Map<String, Object>> chats = (List<Map<String, Object>>) documentSnapshot.get("chats");

                    if (chats != null) {
                        displayMessages(chats);
                    }
                }
            });
    }

    private void displayMessages(List<Map<String, Object>> chats) {
        LinearLayout chatLayout = findViewById(R.id.chatLayout); // LinearLayout where messages will be displayed
        chatLayout.removeAllViews(); // Clear previous messages

        for (Map<String, Object> chat : chats) {
            String messageText = (String) chat.get("text");
            String senderID = (String) chat.get("sender");

            // Inflate the appropriate layout based on the sender
            View messageView;
            if (currentUserID.equals(senderID)) {
                messageView = getLayoutInflater().inflate(R.layout.layout_messages_sender, chatLayout, false);
                TextView senderText = messageView.findViewById(R.id.sender_text);
                senderText.setText(messageText);
            } else {
                messageView = getLayoutInflater().inflate(R.layout.layout_messages_receiver, chatLayout, false);
                TextView receiverText = messageView.findViewById(R.id.receiver_text);
                receiverText.setText(messageText);
            }

            // Add the inflated view to the chat layout
            chatLayout.addView(messageView);
        }
    }

    private void showChatNotification(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_chat_notification);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);

        MaterialButton btnOk = dialog.findViewById(R.id.button_ok);
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

}