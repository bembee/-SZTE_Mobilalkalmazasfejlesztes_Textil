package com.example.textil2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHandler {

    private final int NOTIFICATIONS_ID = 0;
    private static final String CHANNEL_ID = "shop_notification_channel";
    private NotificationManager mNotificationManager;


    private Context mContext;

    public NotificationHandler(Context context) {
        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.mContext = context;
        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Shop Notification", NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("Notifications from TextilKincstár");
        this.mNotificationManager.createNotificationChannel(channel);

    }

    public void send(String message){
        Intent intent = new Intent(mContext, MainActivity.class);

        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        } else {
            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID).setContentTitle("Térjen vissza máskor is")
                .setContentText(message)
                .setSmallIcon(R.drawable.baseline_shopping_cart_24).setContentIntent(pendingIntent)
                .setAutoCancel(true);

        this.mNotificationManager.notify(NOTIFICATIONS_ID, builder.build());
    }
}
