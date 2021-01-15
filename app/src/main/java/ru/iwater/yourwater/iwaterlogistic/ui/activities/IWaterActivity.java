package ru.iwater.yourwater.iwaterlogistic.ui.activities;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.Services.TimeListenerService;
import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentNotificationHistory;
import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentWayLists;
import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentContainer;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class IWaterActivity extends AppCompatActivity {
    static final int NOTIFICATION_ID = 100;
    public final static String CHANNEL_ID = "ru.iwather.yourwater.notification";
    private BottomNavigationView bottomNavigation;//нижняя навигация
    private int position = 0;
    public static float beznalCash = 0.0F;
    public static float nalCash = 0.0F;
    private static NotificationManager notificationManager;
    private static NotificationChannel notificationChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders_list);
        SharedPreferencesStorage.init(getApplicationContext());
        bottomNavigation = findViewById(R.id.bottomNavigation);
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if (googleAPI.isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
            googleAPI.makeGooglePlayServicesAvailable(this);
        }

        //запуск сервиса по прослушиванию системного времени
        Intent service = new Intent(this.getApplicationContext(), TimeListenerService.class);
//        if (Build.VERSION.SDK_INT < 26) {
        this.startService(service);
//        } else {
//            startForegroundService(service);
//        }

        //region роверка подключения к Интернету
        if (!Check.checkInternet(getApplicationContext())) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.CheckInternet, Toast.LENGTH_LONG);
            toast.show();
        } else {
            //region проверка работоспособности сервера
            if (!Check.checkServer(getApplicationContext())) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.CheckServer, Toast.LENGTH_LONG);
                toast.show();
            }
            //endregion
        }
        //endregion

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);

        //выбираем по умолчанию вкладку Заказы
        bottomNavigation.getMenu().getItem(1).setChecked(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.Container, FragmentContainer.newInstance(position)).commit();

        //region обработка нажатия по пунктам навигации внизу экрана
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.waysList:
                        getSupportFragmentManager().beginTransaction().replace(R.id.Container, FragmentWayLists.newInstance()).commit();
                        bottomNavigation.getMenu().getItem(0).setChecked(true);
                        break;
                    case R.id.orders:
                        getSupportFragmentManager().beginTransaction().replace(R.id.Container, FragmentContainer.newInstance(position)).commit();
                        bottomNavigation.getMenu().getItem(1).setChecked(true);
                        break;
                    case R.id.history:
                        getFragmentManager().beginTransaction().replace(R.id.Container, FragmentNotificationHistory.newInstance()).commit();
                        bottomNavigation.getMenu().getItem(2).setChecked(true);
                        break;
                }
                return false;
            }
        });
        //endregion

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu_logout, menu);
        return true;
    }

    //нажатие по кнопке выхода
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout1:
                AlertDialog logoutDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.confirmLogout)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(IWaterActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                SharedPreferencesStorage.clearAll();
                                Helper.stopServices(getApplicationContext());
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();

                logoutDialog.show();
                break;
            case R.id.report_driver_btn:
                android.app.AlertDialog report = new android.app.AlertDialog.Builder(this)
                        .setTitle("Итого:")
                        .setMessage("Наличные: " + nalCash + "\nБезналичные: " + beznalCash)
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
                report.show();

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    public static boolean notificationInit(Context context, String text) {
        Intent notificationIntent = new Intent(context, IWaterActivity.class);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationChannel = new NotificationChannel(CHANNEL_ID, "test", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.GREEN);
        notificationChannel.enableVibration(true);
        notificationManager.createNotificationChannel(notificationChannel);

        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_notification_small)
//                    .setColor(cntx.getResources().getColor(R.color.colorPrimary))
                .setContentText(text) // Текст уведомления
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_square))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true); // автоматически закрыть уведомление после нажатия

        Notification notification = builder.build();

        notificationManager.notify(NOTIFICATION_ID, notification);
        return true;
    }


}
