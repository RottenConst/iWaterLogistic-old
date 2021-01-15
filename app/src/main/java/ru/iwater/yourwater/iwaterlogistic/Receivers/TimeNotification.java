package ru.iwater.yourwater.iwaterlogistic.Receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ru.iwater.yourwater.iwaterlogistic.NotificationSender;
import ru.iwater.yourwater.iwaterlogistic.domain.NotificationOrder;
import ru.iwater.yourwater.iwaterlogistic.remote.DriverWayBill;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class TimeNotification extends BroadcastReceiver {

    private final List<NotificationOrder> notifyOrders = new ArrayList<>();
    private String[] splitPeriod;//разделение временного периода, например 9:00-12:00 по "-"
    private String[] formatedDate;//форматированная дата
    NotificationSender notificationSender;
    int ordersCount;
    private String id;
    private String session;

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferencesStorage.init(context);
        notificationSender = new NotificationSender(context);
        if (SharedPreferencesStorage.checkProperty("session")){
            session = SharedPreferencesStorage.getProperty("session");
//            Log.d("notif", "session = " + session);
        }
        int h = 6;//количество путевых листов по умолчанию
        if (SharedPreferencesStorage.checkProperty("amountOfLists"))
            h = Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists"));
        for (int k = 0; k < h; k++) {
            if (SharedPreferencesStorage.checkProperty("waybill" + k)) {
                id = SharedPreferencesStorage.getProperty("id" + k);
//                Log.d("notif", "id = " + id);
                ordersCount = soapToJSON(loadOrders(context)).length();
                writeToArrays(k);
                if (notifyOrders.get(k) != null) {

                    for (int i = 0; i < notifyOrders.size(); i++) {
                        if (notifyOrders.get(i).getStatus().equals("0") && !notifyOrders.get(i).getPeriod().equals("")) {
                            splitPeriod = notifyOrders.get(i).getPeriod().replaceAll("\\s+", "").split("-");
//                            Log.d("notif", "notyfyOrder " + notifyOrders.size());
                            formatedDate = notifyOrders.get(i).getDate().replaceAll("\\s+", "").split("\\.");
//                            Log.d("notif", "formatedDate" + Arrays.toString(formatedDate));
                            if (timeDifference(splitPeriod[1], formatedDate) <= 3600 && timeDifference(splitPeriod[1], formatedDate) > 1800) {//за 15 минут
                                if (!notifyOrders.get(i).notify) {
                                    notificationSender.sendNotification("Через 1 час истекает заказ №" + notifyOrders.get(i).getId(), i, notifyOrders.get(i).getNotify());
                                    Helper.storeNotification(context, "Через 1 час истекает заказ №" + notifyOrders.get(i).getId(), i + "list" + k);
                                    context.sendBroadcast(new Intent("ru.yourwater.iwaterlogistic.UPDATE_NOTIFICATIONS"));
                                    notifyOrders.get(i).notify = true;
                                }
                            } else if (timeDifference(splitPeriod[1], formatedDate) < 0) {//время вышло
                                if (!notifyOrders.get(i).isFail) {
                                    notificationSender.sendNotification("Время отгрузки заказа №" + notifyOrders.get(i).getId() + " истекло", -i, notifyOrders.get(i).isFail);
                                    Helper.storeNotification(context, "Время отгрузки заказа №" + notifyOrders.get(i).getId() + " истекло", -i + "list" + k);
                                    context.sendBroadcast(new Intent("ru.yourwater.iwaterlogistic.UPDATE_NOTIFICATIONS"));
                                    notifyOrders.get(i).isFail = true;
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    //парсинг json массива и запись данных в динамические массивы
    private void writeToArrays(int k) {
        try {
            JSONArray waybill = new JSONArray(SharedPreferencesStorage.getProperty("waybill" + k));
            if (ordersCount > notifyOrders.size()) {
                notificationSender.sendNotification("Появились новые заказы, пожалуйста обновите список заказов", 0, false);
                notifyOrders.clear();
                for (int j = 0; j < waybill.length(); j++) {
                    notifyOrders.add(new NotificationOrder(waybill.getJSONObject(j).getString("id"),
                            waybill.getJSONObject(j).getString("time"),
                            waybill.getJSONObject(j).getString("status"),
                            waybill.getJSONObject(j).getString("date")));
                }
            } else return;
        } catch (JSONException e) {
            Log.e("iWaterlogistic/json", "Получено исключение", e);
        }
    }


    //вычетание дат текущей и заказа
    private long timeDifference(String time, String[] formatedDate) {

        long diff = 0;
        String date = "";

        if (time.replaceAll("\\s+", "").equals("00:00"))
            time = "24:00";

        date += formatedDate[2] + "-" + formatedDate[1] + "-" + formatedDate[0];
        String orderTime = date.replaceAll("\\s+", "") + " " + time.replaceAll("\\s+", "");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date1 = dateFormat.parse(orderTime);
            diff = (date1.getTime() - System.currentTimeMillis()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return diff;
    }

    private SoapObject loadOrders(Context context) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
                    //загрузка путевого листа
                    Log.d("Order", "session = " + session + " id= " + id);
                    DriverWayBill driverWayBill = new DriverWayBill(session, id);
                    driverWayBill.execute();
//                    Log.d("notif", "ORDERS INFO = " + driverWayBill.get());
                    return driverWayBill.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
        return null;
    }

    private JSONArray soapToJSON(SoapObject ordersSoap) {
        JSONArray jsonArrayOrder = new JSONArray();
        int id = 0, name = 0, order = 0, cash = 0, cash_b = 0, time = 0, contact = 0, notice = 0, date = 0, period = 0, address = 0, coords = 0, status = 0;
        for (int f = 0; f < ordersSoap.getPropertyCount() / 13; f++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("num", String.valueOf(f + 1));
                if (!ordersSoap.getPropertyAsString(id).equals("anyType{}"))
                    jsonObject.put("id", ordersSoap.getPropertyAsString(id));
                else
                    jsonObject.put("id", "");

                if (!ordersSoap.getPropertyAsString(name + 1).equals("anyType{}"))
                    jsonObject.put("name", ordersSoap.getPropertyAsString(name + 1));
                else
                    jsonObject.put("name", "");

                if (!ordersSoap.getPropertyAsString(order + 2).equals("anyType{}"))
                    jsonObject.put("order", ordersSoap.getPropertyAsString(order + 2));
                else
                    jsonObject.put("order", "");

                if (!ordersSoap.getPropertyAsString(cash + 3).equals("anyType{}"))
                    jsonObject.put("cash", ordersSoap.getPropertyAsString(cash + 3));
                else
                    jsonObject.put("cash", "0.00");

                if (!ordersSoap.getPropertyAsString(cash_b + 4).equals("anyType{}"))
                    jsonObject.put("cash_b", ordersSoap.getPropertyAsString(cash_b + 4));
                else
                    jsonObject.put("cash_b", "0.00");

                if (!ordersSoap.getPropertyAsString(time + 5).equals("anyType{}"))
                    jsonObject.put("time", ordersSoap.getPropertyAsString(time + 5));
                else
                    jsonObject.put("time", "");

                if (!ordersSoap.getPropertyAsString(contact + 6).equals("anyType{}"))
                    jsonObject.put("contact", ordersSoap.getPropertyAsString(contact + 6));
                else
                    jsonObject.put("contact", "");

                if (!ordersSoap.getPropertyAsString(notice + 7).equals("anyType{}"))
                    jsonObject.put("notice", ordersSoap.getPropertyAsString(notice + 7));
                else
                    jsonObject.put("notice", "");

                if (!ordersSoap.getPropertyAsString(date + 8).equals("anyType{}"))
                    jsonObject.put("date", ordersSoap.getPropertyAsString(date + 8).replaceAll("/+", "\\."));
                else
                    jsonObject.put("date", "");

                if (!ordersSoap.getPropertyAsString(period + 9).equals("anyType{}"))
                    jsonObject.put("period", ordersSoap.getPropertyAsString(period + 9));
                else
                    jsonObject.put("period", "");

                if (!ordersSoap.getPropertyAsString(address + 10).equals("anyType{}"))
                    jsonObject.put("address", ordersSoap.getPropertyAsString(address + 10));
                else
                    jsonObject.put("address", "");

                if (!ordersSoap.getPropertyAsString(coords + 11).equals("anyType{}"))
                    jsonObject.put("coords", ordersSoap.getPropertyAsString(coords + 11));
                else
                    jsonObject.put("coords", "");

                if (!ordersSoap.getPropertyAsString(status + 12).equals("anyType{}"))
                    jsonObject.put("status", ordersSoap.getPropertyAsString(status + 12));
                else
                    jsonObject.put("status", "");

                jsonArrayOrder.put(jsonObject);
//                Log.d("FragmentOrders", "ORDERS" + jsonArrayOrder.getString(f));
                id += 13;
                name += 13;
                order += 13;
                cash += 13;
                cash_b += 13;
                time += 13;
                contact += 13;
                notice += 13;
                date += 13;
                period += 13;
                address += 13;
                coords += 13;
                status += 13;
            } catch (Exception e) {
                Log.e("iWaterlogistic/json", "Получено исключение", e);
            }

        }

        return jsonArrayOrder;

    }
}