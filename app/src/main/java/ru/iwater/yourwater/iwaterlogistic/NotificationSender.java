package ru.iwater.yourwater.iwaterlogistic;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import ru.iwater.yourwater.iwaterlogistic.ui.activities.IWaterActivity;

public class NotificationSender {
    private static final String CHANEL_ID = "ru.iwather.yourwater.notification";
    Intent notificationIntent;
    NotificationManager notificationManager;
    NotificationChannel notificationChannel;
    PendingIntent contentIntent;

    private Context context;
    public NotificationSender(Context context){
        this.context = context;
        notificationIntent = new Intent(context, IWaterActivity.class);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        createChanelIfNeeded(notificationManager, CHANEL_ID);
    }


    @SuppressLint("NewApi")
    public void sendNotification(String text, int NOTIFY_ID, boolean isNotify){

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification.Builder builder = new Notification.Builder(context);
            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setColor(context.getResources().getColor(R.color.colorPrimary))
                    .setContentText(text) // Текст уведомления
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_square))
                    .setWhen(System.currentTimeMillis())
                    .setStyle(new Notification.BigTextStyle().bigText(text))
                    .setAutoCancel(true); // автоматически закрыть уведомление после нажатия

        Notification notification = builder.build();

        assert notificationManager != null;
        if (isNotify) {
            notificationManager.cancel(NOTIFY_ID);
        } else {
            notificationManager.notify(NOTIFY_ID, notification);
        }
    }

    public void createChanelIfNeeded(NotificationManager manager, String ChanelID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(ChanelID, ChanelID, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            manager.createNotificationChannel(notificationChannel);
        }
    }
}
