package ru.iwater.yourwater.iwaterlogistic.ui.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.domain.Order;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.map.MapActivity;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class AboutOrder extends AppCompatActivity {

    private static final String TAG = "ABOUT_ORDER";

    //region ПЕРЕМЕННЫЕ
    private final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;//для запроса разрешения на звонок
    private String orderTitle = "";//номер , дата, время, адрес заказа
    private String[] phones;//номера телефонов клиента
    private final Order order = new Order();
    private String coord;
    private String times;
    private String period;
    private int position;
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
        //кнопка Отгрузить заказ
        Button confirmOrderBt = findViewById(R.id.button2);
        //endregion

        Intent intent = getIntent();
        SharedPreferencesStorage.init(getApplicationContext());

/////////////////////////////////////////////////////////////////////
        orderTitle = intent.getStringExtra("order");
        order.setId(intent.getStringExtra("order_id"));
        order.setCoords(intent.getStringExtra("coords"));
        order.setOrder(intent.getStringExtra("order_content"));
        order.setCash(intent.getStringExtra("cash"));
        order.setCash_b(intent.getStringExtra("cashb"));
        order.setName(intent.getStringExtra("name"));
        order.setContact(intent.getStringExtra("contact"));
        order.setNotice(intent.getStringExtra("notice"));
        order.setStatus(intent.getStringExtra("status"));
        position = intent.getIntExtra("position", 0);

        coord = intent.getStringExtra("coord");
        times = intent.getStringExtra("times");
        period = intent.getStringExtra("period");

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

        contactsTV.setText(order.getName() + "\n" + order.getContact());

        noteTV.setText(order.getNotice());
        //endregion

        //region просмотр адреса на картах
        lookAtMapTV.setOnClickListener(v -> {
            Intent intent1 = new Intent(AboutOrder.this, MapActivity.class);
            intent1.putExtra("coordinates", coord);
            intent1.putExtra("times", times);
            intent1.putExtra("period", period);
            startActivity(intent1);
        });
        //endregion
        if (order.getStatus().equals("1")) {
            confirmOrderBt.setVisibility(View.GONE);
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
}
