package ru.iwater.yourwater.iwaterlogistic.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.domain.Order;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.map.MapRoute;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class AboutOrder extends AppCompatActivity {

    private static final String TAG = "ABOUT_ORDER";

    //region ПЕРЕМЕННЫЕ
    private final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;//для запроса разрешения на звонок
    private String orderTitle = "";//номер , дата, время, адрес заказа
    private String[] phones;//номера телефонов клиента
    private final Order order = new Order();
    private int position;
    private String myPosition;
    private LocationManager locationManager;
    //endregion

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_order);
        //region кнопка возврата на предыдущий activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //endregion

        //region инициализируем элементы разметки
        //строка, куда будет вставлен order
        TextView orderDataTV = findViewById(R.id.textView7);
        //позиции в заказе и их количества
        TextView orderContentTV = findViewById(R.id.textView9);
        //стоимость заказа
        TextView costTV = findViewById(R.id.textView10);
        ImageView iconCost = findViewById(R.id.imageView4);
        //онтактные данные заказчика
        TextView contactsTV = findViewById(R.id.textView11);
        //примечание от заказчика
        TextView noteTV = findViewById(R.id.textView12);
        //кнопка Посмотреть на карте
        TextView lookAtMapTV = findViewById(R.id.textView13);
        TextView tvCopyAddress = findViewById(R.id.tv_copy_address);
        TextView tvCallNumbers = findViewById(R.id.tv_call_number);
        //кнопка Отгрузить заказ
        Button confirmOrderBt = findViewById(R.id.button2);
        Button callClient = findViewById(R.id.btn_call_client);
        //endregion

        Intent intent = getIntent();
        SharedPreferencesStorage.init(getApplicationContext());
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

/////////////////////////////////////////////////////////////////////
        orderTitle = intent.getStringExtra("order");
        order.setId(intent.getStringExtra("order_id"));
        order.setCoords(intent.getStringExtra("coords"));
        order.setOrder(intent.getStringExtra("order_content"));
        order.setCash(intent.getStringExtra("cash"));
        order.setCash_b(intent.getStringExtra("cashb"));
        order.setName(intent.getStringExtra("name"));
        order.setContact(intent.getStringExtra("contact"));
        order.setTime(intent.getStringExtra("time"));
        order.setNotice(intent.getStringExtra("notice"));
        order.setStatus(intent.getStringExtra("status"));
        order.setAddress(intent.getStringExtra("address"));

        orderDataTV.setText(orderTitle);

        String[] orderArr;

        //region запись данных заказа в поля

        if (!order.getOrder().equals("")) {
            orderArr = order.getOrder().split("\n");
            StringBuilder content = new StringBuilder();
            for (int i = 1; i < orderArr.length; i++)
                content.append(orderArr[i].replaceAll("x", "")).append(" шт.").append("\n");
            orderContentTV.setText(content.toString());
        }
        if (!order.getCash().equals("0.00") && order.getCash() != null) {
            costTV.setText("Наличные: " + order.getCash());
        } else if (!order.getCash_b().equals("0.00") && order.getCash_b() != null) {
            costTV.setText("Безналичные: " + order.getCash_b());
            iconCost.setImageResource(R.drawable.ic_baseline_monetization_on_24);
        } else costTV.setText("-");

        contactsTV.setText(order.getName() +";" + "\n" + order.getAddress() + ";");
        tvCallNumbers.setText(order.getContact());
        noteTV.setText(order.getNotice());
        //endregion

        tvCopyAddress.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", order.getAddress());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "адресс скопирован", Toast.LENGTH_LONG).show();
        });

        //region просмотр адреса на картах
        lookAtMapTV.setOnClickListener(v -> {
            Intent intent1 = new Intent(AboutOrder.this, MapRoute.class);
            intent1.putExtra("coordinates", order.getCoords());
            intent1.putExtra("address", order.getAddress());
            intent1.putExtra("time", order.getTime());
            intent1.putExtra("origin", myPosition);
            startActivity(intent1);
        });
        //endregion
        if (order.getStatus().equals("1")) {
            confirmOrderBt.setVisibility(View.GONE);
            lookAtMapTV.setVisibility(View.GONE);
        }
        //region отгрузка заказа
        confirmOrderBt.setOnClickListener(v -> {
            Intent intent12 = new Intent(AboutOrder.this, ShipmentData.class);
            intent12.putExtra("order_id", order.getId());
            intent12.putExtra("order", orderTitle);
            intent12.putExtra("cash", order.getCash());
            intent12.putExtra("cashb", order.getCash_b());
            intent12.putExtra("position", position);
            startActivity(intent12);
        });

        callClient.setOnClickListener(v -> {
            if (!order.getContact().equals("")) {
                if (order.getContact().contains(","))
                    phones = order.getContact().split(",");
                else if (order.getContact().contains(";"))
                    phones = order.getContact().split(";");
                else {
                    phones = new String[1];
                    phones[0] = order.getContact();
                }
            }

            Intent intentCall = new Intent(Intent.ACTION_CALL);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AboutOrder.this,
                        new String[]{android.Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            } else {
                final AlertDialog aboutDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.makeCall)
                        .setPositiveButton("ОК", (dialog, which) -> {
                            if (intentCall.getData() != null) {
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    startActivity(intentCall);
                                }
                            }
                        })
                        .setNegativeButton("Отмена", (dialog, which) -> dialog.cancel())
                        .setSingleChoiceItems(phones, -1, (dialog, item1) -> {
                            if (phones != null)
                                intentCall.setData(Uri.parse("tel:" + phones[item1]));
                        }).create();

                aboutDialog.show();
            }
        });
        //endregion

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);

        //region проверяем поддержку устройством телефонии
        PackageManager packageManager = getPackageManager();

        boolean telephonySupported = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        boolean gsmSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_GSM);
        boolean cdmaSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA);

        menu.getItem(1).setVisible(telephonySupported || gsmSupported || cdmaSupported);
        //endregion

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private void locationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
    }

    //нажатие по кнопке перехода назад, звонка и выхода
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (!order.getContact().equals("")) {
            if (order.getContact().contains(","))
                phones = order.getContact().split(",");
            else if (order.getContact().contains(";"))
                phones = order.getContact().split(";");
            else {
                phones = new String[1];
                phones[0] = order.getContact();
            }
        }
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.Call:
                final Intent intent = new Intent(Intent.ACTION_CALL);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AboutOrder.this,
                            new String[]{android.Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                } else {
                    final AlertDialog aboutDialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.makeCall)
                            .setPositiveButton("ОК", (dialog, which) -> {
                                if (intent.getData() != null) {
                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    } else
                                        startActivity(intent);
                                }
                            })
                            .setNegativeButton("Отмена", (dialog, which) -> dialog.cancel())
                            .setSingleChoiceItems(phones, -1, (dialog, item1) -> {
                                if (phones != null)
                                    intent.setData(Uri.parse("tel:" + phones[item1]));
                            }).create();

                    aboutDialog.show();
                }
                break;
            case R.id.logout:
                AlertDialog logoutDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.confirmLogout)
                        .setPositiveButton("Да", (dialog, which) -> {
                            Intent intent1 = new Intent(AboutOrder.this, LoginActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent1);
                            SharedPreferencesStorage.init(getApplicationContext());
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
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final Intent intent = new Intent(Intent.ACTION_CALL);
                    final AlertDialog aboutDialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.makeCall)
                            .setPositiveButton("ОК", (dialog, which) -> {
                                if (intent.getData() != null) {
                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    } else
                                        startActivity(intent);
                                }
                            })
                            .setNegativeButton("Отмена", (dialog, which) -> dialog.cancel())
                            .setSingleChoiceItems(phones, -1, (dialog, item) -> {
                                if (phones != null)
                                    intent.setData(Uri.parse("tel:" + phones[item]));
                            }).create();

                    aboutDialog.show();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AboutOrder.this, android.Manifest.permission.CALL_PHONE)) {
                        final AlertDialog explanation = new AlertDialog.Builder(this)
                                .setMessage(R.string.explanation)
                                .setPositiveButton("Отмена", (dialog, which) -> ActivityCompat.requestPermissions(AboutOrder.this,
                                        new String[]{android.Manifest.permission.CALL_PHONE},
                                        MY_PERMISSIONS_REQUEST_CALL_PHONE))
                                .setNegativeButton("Запретить", (dialog, which) -> dialog.cancel()).create();
                        explanation.show();
                    } else {
                        final AlertDialog navigator = new AlertDialog.Builder(this)
                                .setMessage(R.string.navigator)
                                .setNegativeButton("Закрыть", (dialog, which) -> dialog.cancel()).create();
                        navigator.show();
                    }
                }
                return;
            }

        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            getLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            if (ActivityCompat.checkSelfPermission(AboutOrder.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AboutOrder.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AboutOrder.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            getLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void getLocation(Location location) {
        if (location == null)
            return;

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)){
            myPosition = location.getLatitude() + "," +location.getLongitude();
            Log.d("location", myPosition);
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            myPosition = location.getLatitude() + "," +location.getLongitude();
            Log.d("location", myPosition);
        }
    }
}
