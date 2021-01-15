package ru.iwater.yourwater.iwaterlogistic.Services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import ru.iwater.yourwater.iwaterlogistic.Receivers.TimeNotification;

public class TimeListenerService extends Service {

    private TimeNotification timeNotification;//отслеживание системного времени

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Time", "10000000000000000000000000000000000");
        timeNotification = new TimeNotification();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(timeNotification,intentFilter);

        Log.d("TimeNotification","START SERVICE");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("TimeNotification","STOP SERVICE");
        if(timeNotification != null)
            unregisterReceiver(timeNotification);
    }
}
