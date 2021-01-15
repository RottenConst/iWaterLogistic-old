package ru.iwater.yourwater.iwaterlogistic.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.Services.LocationService;

public class RestartLocation extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Check.checkInternet(context)){
            Intent service = new Intent(context, LocationService.class);
            context.startService(service);
        }
    }

}
