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

import UserHomePageDirectory.MainDashboardUser;

public class NotificationService extends Service {
    private String userId;
    private DataSnapshot previousSnapshot;

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
