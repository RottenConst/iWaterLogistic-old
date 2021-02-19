package ru.iwater.yourwater.iwaterlogistic.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import ru.iwater.yourwater.iwaterlogistic.domain.ReportOrder;
import ru.iwater.yourwater.iwaterlogistic.remote.TypeClient;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.Services.CheckServerService;
import ru.iwater.yourwater.iwaterlogistic.Services.NetworkMonitorService;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;
import ru.iwater.yourwater.iwaterlogistic.utils.TypeCash;

public class ShipmentData extends AppCompatActivity {

    //region ПЕРЕМЕННЫЕ
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private String order_id = "";//id заказа
    private String order = "";//№, дата, время и адрес заказа
    private TextView orderRow;//строка с данными о заказе
    private EditText tank;//количество бутылок к возврату
    private TextView error;//ошибка незаполнения поля
    private TextView shipmentTime;//время отгрузки
    private String cash;
    private String cash_b;
    private RadioGroup radioCashGroup;
    private ReportOrder reportOrder;
    private CheckBox docYes;
    private CheckBox docNo;
    private TextView yorInfoTV;
    private ImageView yorInfoIV;
    private EditText comment;//примечания от водителя
    private String latitude = "";//широта
    private String longitude = "";//долгота
    private LocationManager lm;
    private Accept accept;//подтверждение заказа
    private String coords = "";//координаты в формате 23.233;45.4342
    private String delinquency = "";//время отгрузки в случае отсутствия сети
    private ProgressBar progressBar;//индикатор отгрузки
    //timer для прекращения кручения progressbar
    private Timer timer;
    private MyTimerTask mTimerTask;
    private int position;//номер путевого листа
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shipment_data);

        //region кнопка возврата на предыдущий activity
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //endregion

        //region инициализация
        Intent intent = getIntent();
        order_id = intent.getStringExtra("order_id");
        order = intent.getStringExtra("order");
        cash = intent.getStringExtra("cash");
        cash_b = intent.getStringExtra("cashb");
        position = intent.getIntExtra("position",0);
        SharedPreferencesStorage.init(getApplicationContext());

        orderRow = findViewById(R.id.address);
        tank = findViewById(R.id.editText2);
        error = findViewById(R.id.textView27);
        comment = findViewById(R.id.editText);
        radioCashGroup = findViewById(R.id.radio_cash_group);
        yorInfoTV = findViewById(R.id.yorInfo);
        yorInfoIV = findViewById(R.id.yurInfoIcon);

        docNo = findViewById(R.id.DocNO);
        docYes = findViewById(R.id.DocYES);
        //отгрузить заказ
        Button completeOrder = findViewById(R.id.yesShipment);
        Button unCompleteOrder = findViewById(R.id.noShipment);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        shipmentTime = findViewById(R.id.textView14);
        progressBar = findViewById(R.id.progressBar);
        //endregion

        docNo.setVisibility(View.GONE);
        docYes.setVisibility(View.GONE);
        yorInfoIV.setVisibility(View.GONE);
        yorInfoTV.setVisibility(View.GONE);

        String typeClient = null;
        SoapObject type = getTypeClient(getBaseContext(), order_id);
        if (type != null) {
            typeClient = type.getPropertyAsString("info_period");
        }

        if (typeClient != null && typeClient.contains("1")) {
            docYes.setVisibility(View.VISIBLE);
            docNo.setVisibility(View.VISIBLE);
            yorInfoIV.setVisibility(View.VISIBLE);
            yorInfoTV.setVisibility(View.VISIBLE);
            radioCashGroup.setVisibility(View.GONE);
            if (cash_b.equals("0.00")) {
                cash_b = cash;
            }
            reportOrder = new ReportOrder(Integer.parseInt(order_id), Float.parseFloat(cash_b), TypeCash.NON_CASH);
//            Log.d("report", "Type of cash" + TypeCash.valueOf(reportOrder.getTypeCash().name()) + " " + reportOrder.getTypeCash().getTitle());
        } else if (cash.equals("0.00") && cash_b != null) {
            radioCashGroup.setVisibility(View.GONE);
            reportOrder = new ReportOrder(Integer.parseInt(order_id), Float.parseFloat(cash_b), TypeCash.NON_CASH);
//            Log.d("report", "Type of cash" + TypeCash.valueOf(reportOrder.getTypeCash().name()) + " " + reportOrder.getTypeCash().getTitle());
        }

        orderRow.setText(order);

        radioCashGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_cash:
                    comment.setText("Оплата наличными, ");
                    if (cash == "0") {
                        cash = cash_b;
                    }
                    reportOrder = new ReportOrder(Integer.parseInt(order_id), Float.parseFloat(cash), TypeCash.CASH);
//                    Log.d("report", "Type of cash " + TypeCash.valueOf(reportOrder.getTypeCash().name()) + " " + reportOrder.getTypeCash().getTitle());
                    break;
                case R.id.radio_on_site:
                    comment.setText("Оплата на сайте, ");
                    if (cash != "0") {
                        cash_b = cash;
                    }
                    reportOrder = new ReportOrder(Integer.parseInt(order_id), Float.parseFloat(cash), TypeCash.ON_SiTE);
//                    Log.d("report", "Type of cash" + TypeCash.valueOf(reportOrder.getTypeCash().name()) + " " + reportOrder.getTypeCash().getTitle());
                    break;
                case R.id.radio_terminal:
                    comment.setText("Оплата через терминал, ");
                    if (cash != "0") {
                        cash_b = cash;
                    }
                    reportOrder = new ReportOrder(Integer.parseInt(order_id), Float.parseFloat(cash_b), TypeCash.ON_TERMINAL);
//                    Log.d("report", "Type of cash" + TypeCash.valueOf(reportOrder.getTypeCash().name()) + " " + reportOrder.getTypeCash().getTitle());
                    break;
                case R.id.radio_transfer:
                    comment.setText("Оплата переводом, ");
                    if (cash != "0") {
                        cash_b = cash;
                    }
                    reportOrder = new ReportOrder(Integer.parseInt(order_id), Float.parseFloat(cash_b), TypeCash.TRANSFER);
//                    Log.d("report", "Type of cash" + TypeCash.valueOf(reportOrder.getTypeCash().name()) + " " + reportOrder.getTypeCash().getTitle());
                    break;
            }
        });

        docYes.setOnClickListener(v -> {
            if (docYes.isChecked()) {
                docNo.setChecked(false);
                comment.setText("Документы подписаны");
            } else comment.setText("");
        });

        docNo.setOnClickListener(v -> {
            if (docNo.isChecked()) {
                docYes.setChecked(false);
                comment.setText("Документы не подписаны");
            } else comment.setText("");
        });
        /*//region изменение количества бутылок к возврату
        tank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tank.setBackgroundColor(getResources().getColor(R.color.transperent));
                error.setVisibility(View.GONE);
                getFragmentManager().beginTransaction().replace(R.id.pickerContainer, FragmentNumberPicker.newInstance(tank.getText().toString())).addToBackStack(null).commit();
            }
        });*/
        //endregion

        tank.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tank.setBackgroundColor(getResources().getColor(R.color.transperent));
                error.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //region отгрузка заказа
        completeOrder.setOnClickListener(v -> {
            if (tank.getText().toString().length()>0 && reportOrder != null) {
                AlertDialog confirm = new AlertDialog.Builder(ShipmentData.this)
                            .setMessage(R.string.confirm)
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int thank = Integer.parseInt(tank.getText().toString());
                                    reportOrder.setThank(thank);
                                    requestLocation();
                                }
                            })
                            .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).create();
                    confirm.show();
                } else {
                    error.setVisibility(View.VISIBLE);
                    tank.setBackgroundColor(getResources().getColor(R.color.shipmentBackground));
                }
        });
        //endregion

        unCompleteOrder.setOnClickListener(v -> {
            onBackPressed();
        });
    }



    // что будет происходить при работе таймера (задача для таймера):
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.noCoords, Toast.LENGTH_LONG);
                    toast.show();
                    if(timer != null){
                        timer.cancel();
                    }
                }});
        }
    }

    //запрос местоположения
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(ShipmentData.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(ShipmentData.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            if(checkEnabled())
                if (lm != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    timer = new Timer();
                    mTimerTask = new MyTimerTask();
                    timer.schedule(mTimerTask, 20000);
                    if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
                        Log.d("NETWORK_PROVIDER******:", "true");
                    }else if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.d("GPS_PROVIDER**********:", "true");
                        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                    }
                }
        }
    }

    private SoapObject getTypeClient(Context context, String order_id) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
                    TypeClient typeClient = new TypeClient(order_id);
                    typeClient.execute();
                    return typeClient.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Произошла ошибка!", e);
                }
            }
        }
        return null;
    }

    //region получение координат и проверка доступности провайдера
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("****************","onLocationChanged");
            if(location != null) {
                if (latitude.equals("") && longitude.equals("")) {
                    latitude = String.valueOf(location.getLatitude()).replaceAll(",", "\\.");
                    longitude = String.valueOf(location.getLongitude()).replaceAll(",", "\\.");
//                    Log.d("******************:", location.getLatitude() + ";" + location.getLongitude());
                    if(!latitude.equals("") && !longitude.equals("")) {
                        progressBar.setVisibility(View.GONE);
                        if(timer != null){
                            timer.cancel();
                        }
                    }
                }
            } else {
//                Log.d("****************","location = null");
                Toast toast = Toast.makeText(getApplicationContext(), R.string.noCoords, Toast.LENGTH_LONG);
                toast.show();
                progressBar.setVisibility(View.GONE);
            }

            if(!latitude.equals("")&&!longitude.equals("")) {
                coords = latitude + "," + longitude;
                if (Check.checkInternet(getApplicationContext())) {
                    if(Check.checkServer(getApplicationContext())) {
                        try {
                            shipmentTime.setVisibility(View.VISIBLE);
                            shipmentTime.setText("Время отгрузки: " + returnDate());
                            accept = new Accept();
                            accept.execute();
                        }catch (Exception e){
                            Log.e("iWaterLogistic","Получено исключение",e);
                        }
                    } else {
                        makeCash(R.string.noServer,false,position);
                    }
                } else {
                    makeCash(R.string.noInternet,true,position);
                }
            }
        }

        //кэширование отгруженных заказов когда нет доступа к интернету или сервер не доступен
        private void makeCash(int stringResource, boolean flag,int position){
            Toast toast = Toast.makeText(getApplicationContext(), stringResource, Toast.LENGTH_LONG);
            toast.show();
            if (!SharedPreferencesStorage.checkProperty("cashOrder"+ position)) {
                SharedPreferencesStorage.addProperty("cashOrder"+ position, String.valueOf(createJSONArray("")));
                Intent service=null;
                if(flag)
                    service = new Intent(getApplicationContext(), NetworkMonitorService.class);
                else
                    service = new Intent(getApplicationContext(), CheckServerService.class);
                startService(service);
            } else {
                SharedPreferencesStorage.addProperty("cashOrder"+ position, createJSONArray(SharedPreferencesStorage.getProperty("cashOrder"+ position)).toString());
            }
            Intent intent = new Intent(ShipmentData.this, IWaterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            latitude = "";
            longitude = "";
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if(status == LocationProvider.OUT_OF_SERVICE )
                Log.d("***************",provider+" OUT_OF_SERVICE");
            if(status == LocationProvider.TEMPORARILY_UNAVAILABLE )
                Log.d("***************",provider+" TEMPORARILY_UNAVAILABLE");
        }

        @Override
        public void onProviderEnabled(String provider) {
        }
        //оповещение о недоступности функции
        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }
    };
    //endregion

    //создание массива для кэширования данных
    private JSONArray createJSONArray(String str){
        Calendar currentdate = Calendar.getInstance();
        String strdate = "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        strdate = formatter.format(currentdate.getTime());
        TimeZone obj = TimeZone.getTimeZone("Europe/Moscow");

        formatter.setTimeZone(obj);
        Date theResult=null;
        try {
            theResult = formatter.parse(strdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (theResult != null) {
            delinquency = String.valueOf(theResult.getTime()/1000);
        }

        try {
            JSONArray jsonArray;
            if(str.equals(""))
                jsonArray = new JSONArray();
            else
                jsonArray = new JSONArray(str);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", order_id);
            jsonObject.put("tank", tank.getText().toString());
            jsonObject.put("comment", comment.getText().toString());
            jsonObject.put("coord", coords);
            jsonObject.put("delinquency", delinquency);
            jsonArray.put(jsonObject);
            return jsonArray;
        } catch (Exception e) {
            Log.e("cashOrderData", "Получено исключение", e);
        }
        return null;
    }

    //проверка доступности Местоположения
    private boolean checkEnabled() {
        if (lm!=null && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast toast = Toast.makeText(getApplicationContext(),R.string.checkGPS, Toast.LENGTH_LONG);
            toast.show();
            longitude="";
            latitude="";
            return false;
        }
        return true;
    }

    //получение текущего времени в unix формате
    private String returnDate() throws ParseException {

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu_logout, menu);
        menu.findItem(R.id.report_driver_btn).setVisible(false);
        return true;
    }

    //нажатие по кнопке перехода назад, звонка и выхода
    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.logout1:
                AlertDialog logoutDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.confirmLogout)
                        .setPositiveButton("Да", (dialog, which) -> {
                            Intent intent = new Intent(ShipmentData.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            SharedPreferencesStorage.clearAll();
                            Helper.stopServices(getApplicationContext());
                        })
                        .setNegativeButton("Нет", (dialog, which) -> dialog.cancel())
                        .create();

                logoutDialog.show();
                break;


            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    //ответ поьзователя на запрос разрешения функции звонка
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   requestLocation();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ShipmentData.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        final AlertDialog explanation = new AlertDialog.Builder(this)
                                .setMessage(R.string.explanationGPS)
                                .setPositiveButton("Отмена", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(ShipmentData.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                                    }
                                })
                                .setNegativeButton("Запретить", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).create();
                        explanation.show();
                    } else {
                        final AlertDialog navigator = new AlertDialog.Builder(this)
                                .setMessage(R.string.navigator)
                                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).create();
                        navigator.show();
                    }
                }
                break;

        }
    }

    public  class Accept extends AsyncTask<Void, Void, Void> {
        private final static String SOAP_ACTION_ACCEEPT = "urn:authuser#accept";
        private final static String METHOD_NAME_ACCEEPT = "accept";
        private final static String NAMESPACE_ACCEEPT = "urn:authuser";

        private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php?wsdl";
//        private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

        private String[] error;

        @Override
        protected Void doInBackground(Void... params) {

            SoapObject Request = new SoapObject(NAMESPACE_ACCEEPT,METHOD_NAME_ACCEEPT);

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            soapEnvelope.setOutputSoapObject(Request);
            Request.addProperty("id", Integer.parseInt(order_id));
            Request.addProperty("tank", Integer.parseInt(tank.getText().toString()));
            Request.addProperty("comment", comment.getText().toString());
            Request.addProperty("coord", coords);
            Request.addProperty("delinquency", "");
            HttpTransportSE httpTransport = new HttpTransportSE(URL);

            Log.d("ru.iwater.yourwater", Request.toString());


            List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
            HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
            headers.add(headerProperty);

            try{
                httpTransport.call(SOAP_ACTION_ACCEEPT,soapEnvelope,headers);
                String resultString = soapEnvelope.getResponse().toString();
                if(resultString!=null)
                    error = resultString.split(",");
            }catch (Exception e) {
                Log.e("iWaterLogistic","Получено исключение",e);
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(error!=null) {
                Log.d("ru.iwater.yourwater", Arrays.toString(error));
                if (error[0].replaceAll("\\D+", "").equals("0")) {
                    SharedPreferencesStorage.addReport("reportOrder", reportOrder.getOrderID(), reportOrder.getCash(), reportOrder.getTypeCash().name(), reportOrder.getThank());
                    Intent intent = new Intent(ShipmentData.this, Complete.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("order_id", order_id);
                    intent.putExtra("order", order);
                    intent.putExtra("position",position);
                    startActivity(intent);
                }else if(error[0].replaceAll("\\D+", "").equals("1")){
                    Toast toast = Toast.makeText(getApplicationContext(),R.string.errorAccept, Toast.LENGTH_LONG);
                    toast.show();
                    Intent intent = new Intent(ShipmentData.this, IWaterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("position",position);
                    startActivity(intent);
                }
            } else{
                Toast toast = Toast.makeText(getApplicationContext(),R.string.errorAccept, Toast.LENGTH_LONG);
                toast.show();
                Intent intent = new Intent(ShipmentData.this, IWaterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        }
    }
}
