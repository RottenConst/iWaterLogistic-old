package ru.iwater.yourwater.iwaterlogistic.Services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import ru.iwater.yourwater.iwaterlogistic.Receivers.NetworkMonitor;
import ru.iwater.yourwater.iwaterlogistic.Receivers.RestartLocation;

public class RestartLocationService extends Service {

    private RestartLocation restartLocation;
    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        restartLocation = new RestartLocation();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(restartLocation,intentFilter);

        Log.d("RestartLocation","RESTART LOCATION_SERVICE");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("RestartLocation","STOP RESTART LOCATION_SERVICE");
        if(restartLocation != null)
            unregisterReceiver(restartLocation);
    }
}
