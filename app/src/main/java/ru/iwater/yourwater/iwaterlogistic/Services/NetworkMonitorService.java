package ru.iwater.yourwater.iwaterlogistic.Services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import ru.iwater.yourwater.iwaterlogistic.Receivers.NetworkMonitor;

public class NetworkMonitorService extends Service {

    private NetworkMonitor networkMonitor;
    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        networkMonitor = new NetworkMonitor();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkMonitor,intentFilter);

        Log.d("NetworkMonitor","START NETWORKSERVICE");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("NetworkMonitor","STOP NETWORKSERVICE");
        if(networkMonitor != null)
            unregisterReceiver(networkMonitor);
    }
}
