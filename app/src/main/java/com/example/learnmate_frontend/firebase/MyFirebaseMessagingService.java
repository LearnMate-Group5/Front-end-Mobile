package com.example.learnmate_frontend.firebase;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.example.learnmate_frontend.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "learnmate_channel";
    private static final String CHANNEL_NAME = "LearnMate Notifications";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "New Token: " + token);
        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Subscribed to topic 'announcements'");
                    } else {
                        Log.e("FCM", "Failed to subscribe to topic", task.getException());
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            Log.d("FCM", "Message received: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        } else if (remoteMessage.getData().size() > 0) {
            Log.d("FCM", "Data Payload: " + remoteMessage.getData().toString());
            sendNotification("New Data", remoteMessage.getData().get("details")); // Lấy details nếu có
        }
    }

    private void sendNotification(String title, String messageBody) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.learnmate_icon) // Đảm bảo icon tồn tại
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
