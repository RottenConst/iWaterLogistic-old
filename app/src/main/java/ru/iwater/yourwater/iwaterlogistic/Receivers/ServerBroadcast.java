package ru.iwater.yourwater.iwaterlogistic.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.Services.CheckServerService;
import ru.iwater.yourwater.iwaterlogistic.Services.NetworkMonitorService;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class ServerBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferencesStorage.init(context);
//        Log.d("***************","отправляю....");

        int h = 6;//количество путевых листов по умолчанию
        if(SharedPreferencesStorage.checkProperty("amountOfLists"))
            h=Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists"));

        for(int j = 0; j<h; j++) {
            if (SharedPreferencesStorage.checkProperty("cashOrder"+String.valueOf(j))) {
                try {
                    JSONArray jsonArray = new JSONArray(SharedPreferencesStorage.getProperty("cashOrder"+String.valueOf(j)));
                    int k = jsonArray.length();
                    for (int i = k - 1; i >= 0; i--) {
                        String order_id = jsonArray.getJSONObject(i).getString("id");
                        String tank = jsonArray.getJSONObject(i).getString("tank");
                        String comment = jsonArray.getJSONObject(i).getString("comment");
                        String coords = jsonArray.getJSONObject(i).getString("coord");
                        String delinquency = jsonArray.getJSONObject(i).getString("delinquency");
                        if (Check.checkInternet(context)) {
                            if (Check.checkServer(context)) {
//                                Log.d("ServerBroadcast", "я уже у цели");
                                Helper.sendOrders(context, order_id, tank, comment, coords, delinquency, jsonArray, i, j);
                                jsonArray = new JSONArray(SharedPreferencesStorage.getProperty("cashOrder"+String.valueOf(j)));
                            }
                        } else {
                            Intent checkServerService = new Intent(context, CheckServerService.class);
                            context.stopService(checkServerService);
                            Intent networkMonitor = new Intent(context, NetworkMonitorService.class);
                            context.startService(networkMonitor);
                        }
                    }
                    if (jsonArray.length() == 0) {
                        Intent checkServerService = new Intent(context, CheckServerService.class);
                        context.stopService(checkServerService);
                        if (SharedPreferencesStorage.checkProperty("cashOrder"+String.valueOf(j)))
                            SharedPreferencesStorage.removeProperty("cashOrder"+String.valueOf(j));
                    }
                    context.sendBroadcast(new Intent("ru.yourwater.iwaterlogistic.UPDATE_MARKING"+String.valueOf(j)));
                } catch (JSONException e) {
                    Log.e("json", "Получено исключение", e);
                }
            }
        }
    }
}
