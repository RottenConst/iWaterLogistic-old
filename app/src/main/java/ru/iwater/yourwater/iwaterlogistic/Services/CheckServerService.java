package ru.iwater.yourwater.iwaterlogistic.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import ru.iwater.yourwater.iwaterlogistic.Receivers.ServerBroadcast;

public class CheckServerService extends Service {

    private AlarmManager am;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CheckServerService","START SERVERSERVICE");
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(this, ServerBroadcast.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT );

        long time = SystemClock.elapsedRealtime() + 5000;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, time,60000, pendingIntent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;//mBinder
    }

    @Override
    public void onDestroy() {
        Log.d("CheckServerService","STOP SERVERSERVICE");
        if(am != null)
            am.cancel(pendingIntent);
    }
}
