package ru.iwater.yourwater.iwaterlogistic.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.MainScreen;
import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private int k;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        SharedPreferencesStorage.init(this);
        if (SharedPreferencesStorage.checkProperty("token"))
            SharedPreferencesStorage.removeProperty("token");
        SharedPreferencesStorage.addProperty("token",token);
        Log.d("iWater", "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        SharedPreferencesStorage.init(getApplicationContext());
        Log.d("MyFirebaseMessaging",SharedPreferencesStorage.getProperty("k"));
        if(SharedPreferencesStorage.checkProperty("k"))
            if(!SharedPreferencesStorage.getProperty("k").equals(""))
                k = Integer.parseInt(SharedPreferencesStorage.getProperty("k"));
            else k=0;
        try {
            if(remoteMessage.getNotification()!=null)
                sendNotification(remoteMessage.getNotification().getBody());
            Helper.storeNotification(getApplicationContext(),remoteMessage.getNotification().getBody(),String.valueOf(666+k));
            k++;
            SharedPreferencesStorage.addProperty("k",String.valueOf(k));
            sendBroadcast(new Intent("ru.yourwater.iwaterlogistic.UPDATE_NOTIFICATIONS"));
        }catch (Exception e) {
            Log.e("Notification","Получено исключение",e);
        }

    }
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainScreen.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "");
        notificationBuilder.setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_square))
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(this.getString(R.string.app_name))
                .setContentIntent(resultPendingIntent)
                .setWhen(System.currentTimeMillis())
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            notificationManager.notify(0, notificationBuilder.build());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
