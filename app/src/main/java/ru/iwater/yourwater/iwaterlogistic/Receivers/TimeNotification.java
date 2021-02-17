  package ru.iwater.yourwater.iwaterlogistic.Receivers;

import android.annotation.SuppressLint;
import android.app.Notification;
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
import ru.iwater.yourwater.iwaterlogistic.remote.DriverTodayList;
import ru.iwater.yourwater.iwaterlogistic.remote.DriverWayBill;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class TimeNotification extends BroadcastReceiver {

    private static List<NotificationOrder> notifyOrders = new ArrayList<>();
    private String[] splitPeriod;//разделение временного периода, например 9:00-12:00 по "-"
    private String[] formatedDate;//форматированная дата
    NotificationSender notificationSender;
    int ordersCount;
    private String id;
    private List<String> isNotifyIds = new ArrayList<>();
    private List<String> isFails = new ArrayList<>();
    private String session;
    private String idDriver;

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferencesStorage.init(context);
        notificationSender = new NotificationSender(context);
        if (SharedPreferencesStorage.checkProperty("session")){
            session = SharedPreferencesStorage.getProperty("session");
            idDriver = SharedPreferencesStorage.getProperty("id");
//            Log.d("notif", "session = " + session);
        }
        int wayListCount = getDriverList(loadTodayList(context));
        int h = 6;//количество путевых листов по умолчанию
        if (SharedPreferencesStorage.checkProperty("amountOfLists" + Helper.returnFormatedDate(0)))
            h = Integer.valueOf(SharedPreferencesStorage.getProperty("amountOfLists" + Helper.returnFormatedDate(0)));
        comparisonWayList(h, wayListCount);
        for (int k = 0; k < h; k++) {
            if (SharedPreferencesStorage.checkProperty("waybill" + k)) {
                id = SharedPreferencesStorage.getProperty("idwaylist" + k);
//                Log.d("notif", "id = " + id);
                notifyOrders = getNotiyfOrderCount(k);
                ordersCount = soapToJSON(loadOrders(context)).length();
                Log.d("notif", "way list#" + k + " orderCount " + ordersCount + " > notifyOrders " + notifyOrders.size());
                writeToArrays();
                if (notifyOrders != null) {
                    for (int i = 0; i < notifyOrders.size(); i++) {
                        if (notifyOrders.get(i).getStatus().equals("0") && !notifyOrders.get(i).getPeriod().equals("")) {
                            splitPeriod = notifyOrders.get(i).getPeriod().replaceAll("\\s+", "").split("-");
                            formatedDate = notifyOrders.get(i).getDate().replaceAll("\\s+", "").split("\\.");
                            if (timeDifference(splitPeriod[1], formatedDate) <= 3600 && timeDifference(splitPeriod[1], formatedDate) > 0) {//за 15 минут
                                if (!notifyOrders.get(i).notify) {
                                    notificationSender.sendNotification("Через 1 час истекает заказ по адрессу " + notifyOrders.get(i).getAddress(), i, notifyOrders.get(i).getNotify());
                                    Helper.storeNotification(context, "Через 1 час истекает заказ по адрессу " + notifyOrders.get(i).getAddress(), i + "list" + k);
                                    context.sendBroadcast(new Intent("ru.yourwater.iwaterlogistic.UPDATE_NOTIFICATIONS"));
                                    isNotifyIds.add(notifyOrders.get(i).getId());
                                }
                            } else if (timeDifference(splitPeriod[1], formatedDate) < 0) {//время вышло
                                if (!notifyOrders.get(i).isFail) {
                                    notificationSender.sendNotification("Время отгрузки заказа по адрессу " + notifyOrders.get(i).getAddress() + " истекло", -i, notifyOrders.get(i).isFail);
                                    Helper.storeNotification(context, "Время отгрузки заказа по адрессу " + notifyOrders.get(i).getAddress() + " истекло", -i + "list" + k);
                                    context.sendBroadcast(new Intent("ru.yourwater.iwaterlogistic.UPDATE_NOTIFICATIONS"));
                                    isFails.add(notifyOrders.get(i).getId());
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    public List<NotificationOrder> getNotiyfOrderCount(int k) {
        try {
            JSONArray waybill = new JSONArray(SharedPreferencesStorage.getProperty("waybill" + k));
            notifyOrders.clear();
            for (int j = notifyOrders.size(); j < waybill.length(); j++) {
                String id = waybill.getJSONObject(j).getString("id");
                String time = waybill.getJSONObject(j).getString("time");
                String address = waybill.getJSONObject(j).getString("address");
                String status = waybill.getJSONObject(j).getString("status");
                String date = waybill.getJSONObject(j).getString("date");
                NotificationOrder notOrder = new NotificationOrder(id, time, address, status, date);
                for (int idNotify = 0; idNotify < isNotifyIds.size(); idNotify++) {
                        if (isNotifyIds.get(idNotify).equals(id)) {
                            notOrder.notify = true;
                        }
                }
                for (int idFail = 0; idFail < isFails.size(); idFail++) {
                    if (isFails.get(idFail).equals(id)) {
                        notOrder.isFail = true;
                    }
                }
                notifyOrders.add(notOrder);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return notifyOrders;
    }

    //сравнение заказов в путевых листах и уведомление
    private void writeToArrays() {
        if (ordersCount > notifyOrders.size()) {
            notificationSender.sendNotification("Появились новые заказы, пожалуйста обновите список заказов", notifyOrders.size() + 100, false);
        }
    }

    private void comparisonWayList(int local, int crm) {
        if (crm > local) {
            notificationSender.sendNotification("Появились новые заказы, пожалуйста обновите список заказов", crm + 200, false);
            SharedPreferencesStorage.addProperty("amountOfLists" + Helper.returnFormatedDate(0), String.valueOf(crm));
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
//                    Log.d("Order", "session = " + session + " id= " + id);
                    DriverWayBill driverWayBill = new DriverWayBill(session, id);
                    driverWayBill.execute();
                    return driverWayBill.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
        return new SoapObject();
    }

    private SoapObject loadTodayList(Context context) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
//                    TodayList todayList = new TodayList(account.getSession()); //для получения всех путивых листов
                    DriverTodayList todayList = new DriverTodayList(idDriver);
                    todayList.execute();
                    return todayList.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
        return null;
    }

    private int getDriverList(SoapObject driverListSoap) {
        String[] wayListsId;
        if (driverListSoap != null) {
            if (driverListSoap.getPropertyAsString("id").contains(",")) {
                wayListsId = driverListSoap.getPropertyAsString("id").replaceAll("\\s+", "").split(",");
                return wayListsId.length;
            }
        }
        return 0;
    }

    private JSONArray soapToJSON(SoapObject ordersSoap) {
        JSONArray jsonArrayOrder = new JSONArray();
        int id = 0, name = 0, order = 0, cash = 0, cash_b = 0, time = 0, contact = 0, notice = 0, date = 0, period = 0, address = 0, coords = 0, status = 0;
        if (ordersSoap != null) {
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
        }
        return jsonArrayOrder;

    }
}