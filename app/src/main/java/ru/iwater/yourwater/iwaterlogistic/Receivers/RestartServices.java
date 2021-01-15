package ru.iwater.yourwater.iwaterlogistic.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.Services.CheckServerService;
import ru.iwater.yourwater.iwaterlogistic.Services.LocationService;
import ru.iwater.yourwater.iwaterlogistic.Services.NetworkMonitorService;
import ru.iwater.yourwater.iwaterlogistic.Services.TimeListenerService;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class RestartServices extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferencesStorage.init(context);
        try {
                if (SharedPreferencesStorage.checkProperty("session")) {
                    Intent location = new Intent(context, LocationService.class);
                    context.startService(location);
//                    Log.d("RestartServices","restart location service");

                    int h = 6;//количество путевых листов по умолчанию
                    if(SharedPreferencesStorage.checkProperty("amountOfLists"))
                        h=Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists"));

                    for (int k=0; k<h; k++) {
                        if (SharedPreferencesStorage.checkProperty("waybill"+String.valueOf(k))){
                            Intent notification = new Intent(context, TimeListenerService.class);
                            context.startService(notification);
//                            Log.d("RestartServices", "restart notification service");
                            break;
                        }
                    }

                    for(int k = 0; k<h; k++) {
                        if (SharedPreferencesStorage.checkProperty("cashOrder"+String.valueOf(k))) {
                            if (Check.checkInternet(context)) {
                                Intent cash = new Intent(context, CheckServerService.class);
                                context.startService(cash);
//                                Log.d("RestartServices", "restart server service");
                            } else {
                                Intent cash = new Intent(context, NetworkMonitorService.class);
                                context.startService(cash);
//                                Log.d("RestartServices", "restart network service");
                            }
                            break;
                        }
                    }
                }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
