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

public class NetworkMonitor extends BroadcastReceiver {

    private String order_id="",tank="",comment="",coords="",delinquency="";//данные для отгрузки заказа

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferencesStorage.init(context);


        int h = 6;//количество путевых листов по умолчанию
        if(SharedPreferencesStorage.checkProperty("amountOfLists"))
            h=Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists"));

        for(int j = 0; j<h; j++) {
            if (SharedPreferencesStorage.checkProperty("cashOrder"+String.valueOf(j))) {
                try {
                    JSONArray jsonArray = new JSONArray(SharedPreferencesStorage.getProperty("cashOrder"+String.valueOf(j)));
                    int k = jsonArray.length();
                    for (int i = k - 1; i >= 0; i--) {
                        order_id = jsonArray.getJSONObject(i).getString("id");
                        tank = jsonArray.getJSONObject(i).getString("tank");
                        comment = jsonArray.getJSONObject(i).getString("comment");
                        coords = jsonArray.getJSONObject(i).getString("coord");
                        delinquency = jsonArray.getJSONObject(i).getString("delinquency");
                        if (Check.checkInternet(context)) {
                            if (Check.checkServer(context)) {
                                Helper.sendOrders(context, order_id, tank, comment, coords, delinquency, jsonArray, i, j);
                                jsonArray = new JSONArray(SharedPreferencesStorage.getProperty("cashOrder"+String.valueOf(j)));
                            } else {
                                Intent networkService = new Intent(context, NetworkMonitorService.class);
                                context.stopService(networkService);
                                Intent checkServerService = new Intent(context, CheckServerService.class);
                                context.startService(checkServerService);
                            }
                        }
                    }

                    if (jsonArray.length() == 0) {
                        Intent networkService = new Intent(context, NetworkMonitorService.class);
                        context.stopService(networkService);
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
