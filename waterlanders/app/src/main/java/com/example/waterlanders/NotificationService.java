package com.example.waterlanders;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import UserHomePageDirectory.FragmentsDirectory.ChatActivity;
import UserHomePageDirectory.MainDashboardUser;

public class NotificationService extends Service {
    private String userId;
    private DataSnapshot previousSnapshot;
    private String previousUserAccountStatus;
    private List<Map<String, Object>> previousChats = null;
    private Set<Map<String, Object>> previousChatsAdmin = new HashSet<>();


    @Override
    public void onCreate() {
        super.onCreate();

        // Notification channel creation (only once)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "CHANNEL_ID",
                    "Data Change Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get userId from intent or FirebaseAuth
        userId = intent.getStringExtra("userId");
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Start as a foreground service to prevent being killed
        startForeground(1, getForegroundNotification());

        if (userId.equals("NVWcwGTdD1VdMcnUg86IBAUuE3i2")){
            FirebaseFirestore fdb = FirebaseFirestore.getInstance();
            boolean[] isInitialLoad = {true};
            Log.d("Notification service admin", "admin notif this is initialize");

            // Listener for chat messages
            fdb.collection("messages").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> data = document.getData();

                        // Get the array of chats
                        List<Map<String, Object>> currentChats = (List<Map<String, Object>>) data.get("chats");
                        if (currentChats != null) {
                            previousChatsAdmin.addAll(currentChats);
                        }
                    }
                    Log.d("Notification service admin", "previousChatsAdmin1: "+previousChatsAdmin);

                    // After fetching initial data, set up the snapshot listener
                    fdb.collection("messages").addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            Log.e("Firestore Listener", "Failed to listen for messages: " + e.getMessage());
                            return;
                        }

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentChange docChange : querySnapshot.getDocumentChanges()) {
                                Map<String, Object> data = docChange.getDocument().getData();
                                List<Map<String, Object>> currentChats = (List<Map<String, Object>>) data.get("chats");

                                if (currentChats != null) {
                                    Log.d("Notification service admin", "previousChatsAdmin2: "+previousChatsAdmin);
                                    for (Map<String, Object> chat : currentChats) {
                                        boolean isNewChat = !previousChatsAdmin.contains(chat);
                                        Log.d("Notification service admin", "chat: "+chat);

                                        if (!isInitialLoad[0] && (docChange.getType() == DocumentChange.Type.ADDED || isNewChat)) {
                                            String senderID = (String) chat.get("sender");
                                            String sender = (String) chat.get("senderName");
                                            String message = (String) chat.get("text");

                                            if (!senderID.equals(userId)) {
                                                sendNotificationAdmin("Water Chat", "From " + sender + ": " + message, senderID);
                                            }
                                        }
                                        previousChatsAdmin.add(chat);
                                    }

                                    // Update the previousChatsAdmin for this document
                                    //previousChatsAdmin = new ArrayList<>(currentChats);
                                }
                            }
                        }
                        isInitialLoad[0] = false;
                    });
                    Log.d("Notification service admin", "previousChatsAdmin3: "+previousChatsAdmin);
                } else {
                    Log.e("Firestore Fetch", "Error fetching initial data: ", task.getException());
                }
            });
        } else {
            // Firebase Database listener for data changes
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(userId).child("orders");
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot currentSnapshot) {
                    if (currentSnapshot.exists()) {
                        if (previousSnapshot != null) {
                            for (DataSnapshot orderSnapshot : currentSnapshot.getChildren()) {
                                String orderId = orderSnapshot.child("orderId").getValue(String.class);

                                // Check if the orderId exists in previousSnapshot
                                DataSnapshot previousOrderSnapshot = previousSnapshot.child(orderId);

                                if (previousOrderSnapshot.exists()) {
                                    // Compare current and previous orderStatus
                                    String currentOrderStatus = orderSnapshot.child("orderStatus").getValue(String.class);
                                    String previousOrderStatus = previousOrderSnapshot.child("orderStatus").getValue(String.class);

                                    if (currentOrderStatus != null && !currentOrderStatus.equals(previousOrderStatus)) {
                                        String formattedNotificationText = "Your order " + orderId + " status changed from " + previousOrderStatus + " to " + currentOrderStatus + "!";
                                        sendNotification("WATERLAND", formattedNotificationText);
                                    }
                                }
                            }
                        }
                        // Update previousSnapshot after processing currentSnapshot
                        previousSnapshot = currentSnapshot;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    String errorMessage = databaseError.getMessage();
                    int errorCode = databaseError.getCode();

                    // Log the error or show a notification to the user
                    Log.e("Notification Service", "Error Code: " + errorCode + ", Message: " + errorMessage);
                }
            });

            // Firebase firestore listener for chat and account status data changes
            // 'messages' collection -> userId document id for chat
            // 'users' collection -> userId document id -> 'accountStatus' field for account status
            // if there are data changes then put notification
            FirebaseFirestore fdb = FirebaseFirestore.getInstance();

            // Listener for chat messages
            fdb.collection("messages")
                    .document(userId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Log.e("Firestore Listener", "Failed to listen for messages: " + e.getMessage());
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Get the current array of chats
                            List<Map<String, Object>> currentChats = (List<Map<String, Object>>) documentSnapshot.get("chats");

                            if (previousChats != null) {
                                for (Map<String, Object> chat : currentChats) {
                                    // Check if this chat already exists in previousChats
                                    if (!previousChats.contains(chat)) {
                                        // New chat found
                                        String senderID = (String) chat.get("sender");
                                        String sender = (String) chat.get("senderName");
                                        String message = (String) chat.get("text");

                                        if (!senderID.equals(userId)) {
                                            sendNotificationAdmin("Water Chat", "From " + sender + ": " + message, senderID);
                                        }
                                    }
                                }
                            }

                            // Update the previousChats to currentChats for future comparisons
                            previousChats = currentChats;
                        }
                    });


            // Listener for account status
            // Add a flag to track the first snapshot
            AtomicBoolean isInitialSnapshot = new AtomicBoolean(true);

            fdb.collection("users")
                    .document(userId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Log.e("Firestore Listener", "Failed to listen for account status: " + e.getMessage());
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String accountStatus = documentSnapshot.getString("accountStatus");

                            // Skip the notification for the initial snapshot
                            if (isInitialSnapshot.get()) {
                                previousUserAccountStatus = accountStatus;
                                isInitialSnapshot.set(false); // Mark the initial snapshot as processed
                                return;
                            }

                            if (!accountStatus.equals(previousUserAccountStatus)){
                                previousUserAccountStatus = accountStatus;
                                sendNotification("Waterland", "Your account status has changed to: " + accountStatus);
                            }
                        }
                    });
        }

        return START_STICKY; // Ensures the service restarts if terminated
    }

    private Notification getForegroundNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("")
                .setContentText("")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        return builder.build();
    }

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, MainDashboardUser.class);
        intent.putExtra("open_fragment", "history");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void sendNotificationUser(String title, String message, String senderID) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiver_id", senderID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void sendNotificationAdmin(String title, String message, String senderID) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiver_id", senderID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up any resources like listeners if needed
    }
}
