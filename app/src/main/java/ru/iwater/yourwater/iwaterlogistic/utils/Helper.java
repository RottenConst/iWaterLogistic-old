package ru.iwater.yourwater.iwaterlogistic.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ru.iwater.yourwater.iwaterlogistic.Services.CheckServerService;
import ru.iwater.yourwater.iwaterlogistic.Services.LocationService;
import ru.iwater.yourwater.iwaterlogistic.Services.NetworkMonitorService;
import ru.iwater.yourwater.iwaterlogistic.Services.TimeListenerService;

public class Helper {

//    region дата в формате 2018-01-01
        public static String returnFormatedDate(int addedDays){

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_YEAR, addedDays);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(calendar.getTime());

            return formattedDate;
        }
        //endregion

        //region кэширование уведомлений
        public static void storeNotification(Context cntx, String text, String id) {
            boolean flag = false;//проверка на наличие уведомления
            SharedPreferencesStorage.init(cntx);
            try {
                if(SharedPreferencesStorage.checkProperty(returnFormatedDate(0))) {//если есть сегодняшняя дата
                    if(!SharedPreferencesStorage.getProperty(returnFormatedDate(0)).equals("")) {//и это поле не равно пустой строке
                        //создаём json массив на основе этого поля
                        JSONArray jsonArray = new JSONArray(SharedPreferencesStorage.getProperty(returnFormatedDate(0)));
                        for(int j=0; j<jsonArray.length(); j++)//проверка на наличие уведомлений в кэше, т.к. сервис срабатывает каждую минуту
                        if(jsonArray.getJSONObject(j).getString("id").equals(id)){
                            flag = true;
                            break;
                        }
                        if(!flag)//если нет такого уведомление, записываем его
                            SharedPreferencesStorage.addProperty(returnFormatedDate(0),makeNotifArray(SharedPreferencesStorage.getProperty(returnFormatedDate(0)),text,id).toString());
                    } else//иначе создаём новый массив
                        SharedPreferencesStorage.addProperty(returnFormatedDate(0),makeNotifArray("",text,id).toString());
                } else {//иначе создаём новое поле с сегодняшней датой
                    SharedPreferencesStorage.addProperty(returnFormatedDate(0),makeNotifArray("",text,id).toString());
                    //в кэше не должно быть больше трёх дней, удаляем все остальные
                    int i=-3;
                    while(SharedPreferencesStorage.checkProperty(returnFormatedDate(i))) {
                        SharedPreferencesStorage.removeProperty(returnFormatedDate(i));
                        i--;
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        //endregion

        //region текущая дата в формате 01/01 00:00
        private static String getCurrentDate(){
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
            String formattedDate = sdf.format(date);

            return formattedDate;
        }
        //endregion

        //region json массив для уведомлений
        private static JSONArray makeNotifArray(String arr,String text, String id) throws JSONException {
            JSONArray jsonArray;
            if(arr.equals(""))
                jsonArray = new JSONArray();
            else
                jsonArray = new JSONArray(arr);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("date",getCurrentDate());
            jsonObject.put("notification",text);
            jsonObject.put("id",id);
            jsonArray.put(jsonObject);

            return jsonArray;
        }
        //endregion

    //region остановка сервисов
    public static void stopServices(Context cntx){
        Intent service = new Intent(cntx, TimeListenerService.class);
        cntx.stopService(service);

        Intent networkService = new Intent(cntx, NetworkMonitorService.class);
        cntx.stopService(networkService);

        Intent locationService = new Intent(cntx, LocationService.class);
        cntx.stopService(locationService);

        Intent checkServerService = new Intent(cntx, CheckServerService.class);
        cntx.stopService(checkServerService);
    }
    //endregion

    //region отправка кэшированных заказов
    public static void sendOrders(Context context,String order_id,String tank,String comment,String coords,String delinquency,JSONArray jsonArray, int i,int pos){
        try {
            Log.d("******************", String.valueOf(jsonArray.length()));
            Accept accept = new Accept(context,order_id,tank,comment,coords,delinquency);
            accept.execute();
            String err = accept.get();
            if (err.equals("0")) {
                if (SharedPreferencesStorage.checkProperty("waybill"+String.valueOf(pos))) {
                    JSONArray statuses = new JSONArray(SharedPreferencesStorage.getProperty("waybill"+String.valueOf(pos)));
                    for (int c = 0; c < statuses.length(); c++) {
                        if (jsonArray.getJSONObject(i).getString("id").equals(statuses.getJSONObject(c).getString("id"))) {
                            statuses.getJSONObject(c).put("status", "1");
                            Log.d("**********statuses", statuses.getJSONObject(c).toString());
                            Log.d("**********statuses", statuses.toString());
                            SharedPreferencesStorage.addProperty("waybill"+String.valueOf(pos), statuses.toString());
                            break;
                        }
                    }
                }
                removeJsonObject(jsonArray, i, pos);
            }
        } catch (Exception e) {
            Log.e("iWater Logistic", "Получено исключение", e);
        }
    }
    //endregion

    //region удаление объектов измассива по мере отгрузки заказов
    public static void removeJsonObject(JSONArray jsonArray, int position, int pos){
        ArrayList<JSONObject> list = new ArrayList<JSONObject>();
        if (jsonArray != null) {
            for (int i=0;i<jsonArray.length();i++){
                try {
                    list.add(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        list.remove(position);
        JSONArray jsArray = new JSONArray(list);
        if(SharedPreferencesStorage.checkProperty("cashOrder"+String.valueOf(pos)))
            SharedPreferencesStorage.removeProperty("cashOrder"+String.valueOf(pos));
        SharedPreferencesStorage.addProperty("cashOrder"+String.valueOf(pos),jsArray.toString());
    }
    //endregion

    //region отгрузка кэшированного заказа
    public static class Accept extends AsyncTask<Void, Void, String> {
        private final static String SOAP_ACTION_ACCEEPT = "urn:authuser#accept";
        private final static String METHOD_NAME_ACCEEPT = "accept";
        private final static String NAMESPACE_ACCEEPT = "urn:authuser";

        private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php?wsdl";
//        private final static String URL = "http://api.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

        private String[] error;
        private Context cntx;
        private String order_id="";
        private String tank="";
        private String comment="";
        private String coords="";
        private String delinquency="";

        public Accept(Context context,String id, String tnk, String comm, String coord, String del){
            cntx = context;
            order_id = id;
            tank = tnk;
            comment = comm;
            coords = coord;
            delinquency = del;
        }

        @Override
        protected String doInBackground(Void... params) {

            SoapObject Request = new SoapObject(NAMESPACE_ACCEEPT,METHOD_NAME_ACCEEPT);

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            soapEnvelope.setOutputSoapObject(Request);
            Request.addProperty("id", Integer.parseInt(order_id));
            Request.addProperty("tank", Integer.parseInt(tank));
            Request.addProperty("comment", comment);
            Request.addProperty("coord", coords);
            Request.addProperty("delinquency", delinquency);
            HttpTransportSE httpTransport = new HttpTransportSE(URL);

            List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
            HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
            headers.add(headerProperty);

            try{
                httpTransport.call(SOAP_ACTION_ACCEEPT,soapEnvelope,headers);
                String resultString = soapEnvelope.getResponse().toString();
                if(resultString!=null)
                    error = resultString.split(",");
            }catch (Exception e) {
                Log.e("iWater","Получено исключение",e);
            }
            return  error[0].replaceAll("\\D+","");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result!=null) {
                if (result.equals("0")) {
                    Toast toast = Toast.makeText(cntx, "Заказ №" + order_id + " выполнен", Toast.LENGTH_LONG);
                    toast.show();
                } else if (result.equals("1")) {
                    Toast toast = Toast.makeText(cntx, "Не удалось отгрузить заказ №" + order_id, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }
    //endregion

    //region текущая дата в формате юникс
    public static String returnUNIXDate(){
        String unixDate="";

        try {
            Calendar currentdate = Calendar.getInstance();
            String strdate = "";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            strdate = formatter.format(currentdate.getTime());
            TimeZone obj = TimeZone.getTimeZone("Europe/Moscow");

            formatter.setTimeZone(obj);

            Date theResult = formatter.parse(strdate);

            unixDate = String.valueOf(theResult.getTime() / 1000);
        }catch (ParseException e){
            e.printStackTrace();
        }

        return unixDate;
    }
    //endregion

}
