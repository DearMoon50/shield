package com.dearmoon.shield.alert;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.dearmoon.shield.R;
import com.dearmoon.shield.ui.AlertActivity;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AlertManager {
    private static final String CHANNEL_ID = "shield_alerts";
    private static final int ALERT_NOTIFICATION_ID = 2000;
    private final Context context;
    private final NotificationManager notificationManager;
    
    @Inject
    public AlertManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Security Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Critical ransomware detection alerts");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public void showHighRiskAlert(String filePath, int confidenceScore) {
        Intent intent = new Intent(context, AlertActivity.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("score", confidenceScore);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ RANSOMWARE DETECTED")
            .setContentText("Confidence: " + confidenceScore + "% | File: " + filePath)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_delete, "Mitigate", pendingIntent);
        
        notificationManager.notify(ALERT_NOTIFICATION_ID, builder.build());
    }
}
