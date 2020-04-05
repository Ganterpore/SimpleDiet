package com.ganterpore.simplediet.Controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.Activities.MainActivity;

public class NotificationReciever extends BroadcastReceiver {
    public static final String TAG = "NotificationReceiver";
    public static final String MORNING_NOTIFICATION_CHANNEL = "morning_notifications";
    public static final int MORNING_NOTIFICATION_ID = 1;

    public static final String EVENING_NOTIFICATION_CHANNEL = "evening_notifications";
    public static final int EVENING_NOTIFICATION_ID = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("id");
        //if there is no id, we cant do anything
        if(id==null) {
            return;
        }
        switch (id) {
            case MORNING_NOTIFICATION_CHANNEL:
                createMorningNotification(context);
                break;
            case  EVENING_NOTIFICATION_CHANNEL:
                createEveningNotification(context);
                break;
        }
    }

    private void createEveningNotification(Context context) {
        String content = "Don't forget to add your dinner and any forgotten meals for the day!";

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EVENING_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.symbol_food_completed_thumbnail)
                .setContentTitle("Good evening!")
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(EVENING_NOTIFICATION_ID, builder.build());
    }

     private void createMorningNotification(Context context) {
        String content = "Have a happy, healthy breakfast! Don't forget to add any meals you may have missed yesterday!";

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MORNING_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.symbol_food_completed_thumbnail)
                .setContentTitle("Good morning!")
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(MORNING_NOTIFICATION_ID, builder.build());
    }

    public static void buildChannels(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            CharSequence name = "Morning Notifications";
            String description = "Morning update notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel morningChannel = new NotificationChannel(MORNING_NOTIFICATION_CHANNEL, name, importance);
            morningChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(morningChannel);

            name = "Evening Notifications";
            description = "Evening update notifications";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel eveningChannel = new NotificationChannel(EVENING_NOTIFICATION_CHANNEL, name, importance);
            eveningChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(eveningChannel);
        }
    }
}
