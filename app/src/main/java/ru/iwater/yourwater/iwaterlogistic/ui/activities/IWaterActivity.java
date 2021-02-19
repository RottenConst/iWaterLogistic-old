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

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.Services.TimeListenerService;
import ru.iwater.yourwater.iwaterlogistic.domain.Report;
import ru.iwater.yourwater.iwaterlogistic.domain.ReportOrder;
import ru.iwater.yourwater.iwaterlogistic.remote.ReportDriverNow;
import ru.iwater.yourwater.iwaterlogistic.remote.ReportInserts;
import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentCompleteOrder;
import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentNotificationHistory;
import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentOrders;
import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentWayLists;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;
import ru.iwater.yourwater.iwaterlogistic.utils.TypeCash;

public class IWaterActivity extends AppCompatActivity {
    static final int NOTIFICATION_ID = 100;
    public final static String CHANNEL_ID = "ru.iwather.yourwater.notification";
    private BottomNavigationView bottomNavigation;//нижняя навигация
    public ReportOrder reportOrders;
    private static Report reportDay;
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


        //выбираем по умолчанию вкладку Заказы
        bottomNavigation.getMenu().getItem(1).setChecked(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.Container, FragmentOrders.newInstance()).commit();

        //region обработка нажатия по пунктам навигации внизу экрана
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.waysList:
                        getSupportFragmentManager().beginTransaction().replace(R.id.Container, FragmentCompleteOrder.newInstance()).commit();
                        bottomNavigation.getMenu().getItem(0).setChecked(true);
                        break;
                    case R.id.orders:
                        getSupportFragmentManager().beginTransaction().replace(R.id.Container, FragmentOrders.newInstance()).commit();
                        bottomNavigation.getMenu().getItem(1).setChecked(true);
                        break;
                    case R.id.history:
                        getSupportFragmentManager().beginTransaction().replace(R.id.Container, FragmentNotificationHistory.newInstance()).commit();
                        bottomNavigation.getMenu().getItem(2).setChecked(true);
                        break;
                }
                return false;
            }
        });
        //endregion
        reportDay = null;
        initReport(this);
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
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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
                        .setTitle("Итого за " + reportDay.getDate())
                        .setMessage("Всего: " + reportDay.getFullCash() +
                                "\n" + TypeCash.CASH.getTitle() + " " + reportDay.getCash() + "р" +
                                "\n" + TypeCash.ON_SiTE.getTitle() + " " + reportDay.getOn_site() + "р" +
                                "\n" + TypeCash.ON_TERMINAL.getTitle() + " " + reportDay.getOn_terminal() + "р" +
                                "\n" + TypeCash.TRANSFER.getTitle() + " " + reportDay.getTransfer() + "р" +
                                "\n" + TypeCash.NON_CASH.getTitle() + " " + reportDay.getNon_cash() + "р" +
                                "\nСдано бутелей: " + reportDay.getTank() +
                                "\n" + "Выполнено заказов " + " " + reportDay.getOrderCount())
                        .setNegativeButton("Ok", (dialog, which) -> dialog.cancel())
                        .create();
                report.show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void initReport(Context context) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        String date = format.format(calendar.getTime());

        if (reportDay == null && reportAnswer(context) == null) {
            reportDay = new Report(date);
        } else if (reportAnswer(context) != null) {
            reportDay = getReportDay(reportAnswer(context));
            if (!reportDay.getDate().equals(date)) {
                reportDay = new Report(date);
            }
        }
        if (SharedPreferencesStorage.checkReportProperty("reportOrder")) {
            reportOrders = SharedPreferencesStorage.getReport("reportOrder");
        }
        if (reportOrders != null) {
            reportDay.setTank(reportOrders.getThank());
            reportDay.setOrderCount(reportDay.getOrderCount() + 1);
            switch (reportOrders.getTypeCash()) {
                case NON_CASH:
                    reportDay.setNon_cash(reportOrders.getCash());
                    reportInsert(this, reportOrders.getTypeCash().getTitle(), reportOrders.getCash(), reportDay.getTank(), reportDay.getOrderCount(), reportDay.getFullCash());
                    break;
                case CASH:
                    reportDay.setCash(reportOrders.getCash());
                    reportInsert(this, reportOrders.getTypeCash().getTitle(), reportOrders.getCash(), reportDay.getTank(), reportDay.getOrderCount(), reportDay.getFullCash());
                    break;
                case ON_SiTE:
                    reportDay.setOn_site(reportOrders.getCash());
                    reportInsert(this, reportOrders.getTypeCash().getTitle(), reportOrders.getCash(), reportDay.getTank(), reportDay.getOrderCount(), reportDay.getFullCash());
                    break;
                case ON_TERMINAL:
                    reportDay.setOn_terminal(reportOrders.getCash());
                    reportInsert(this, reportOrders.getTypeCash().getTitle(), reportOrders.getCash(), reportDay.getTank(), reportDay.getOrderCount(), reportDay.getFullCash());
                    break;
                case TRANSFER:
                    reportDay.setTransfer(reportOrders.getCash());
                    reportInsert(this, reportOrders.getTypeCash().getTitle(), reportOrders.getCash(), reportDay.getTank(), reportDay.getOrderCount(), reportDay.getFullCash());
                    break;
            }
        }
    }

    private void reportInsert(Context context, String payment_type, float payment, int number_containers, int orders_delivered, float total_money) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
                    ReportInserts reportInserts = new ReportInserts(payment_type, payment, number_containers, orders_delivered, total_money);
                    reportInserts.execute();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
//        return null;
    }

    private SoapObject reportAnswer(Context context) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
                    //загрузка путевого листа
                    ReportDriverNow reportInserts = new ReportDriverNow();
                    reportInserts.execute();
                    return reportInserts.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
        return null;
    }

    private Report getReportDay(SoapObject report) {
        String typeCash;
        float cashes = 0;
        float non_cash =0;
        float on_site = 0;
        float transfer = 0;
        float on_terminal = 0;
        String container = "";
        String order = "";
        int type = 0, moneys = 0, tank = 0, orderCount = 0;
        for (int i = 0; i < report.getPropertyCount() / 9; i++) {
            if (!report.getPropertyAsString(type + 2).equals("anyType{}")) {
                typeCash = report.getPropertyAsString(type + 2);
            } else typeCash = "";

            if (TypeCash.CASH.getTitle().equals(typeCash)) {
                if (!report.getPropertyAsString(moneys + 3).equals("anyType{}")) {
                    String money = report.getPropertyAsString(moneys + 3);
                    cashes += Float.parseFloat(money);
                }
            } else if (TypeCash.NON_CASH.getTitle().equals(typeCash)) {
                if (!report.getPropertyAsString(moneys + 3).equals("anyType{}")) {
                    String money = report.getPropertyAsString(moneys + 3);
                    non_cash += Float.parseFloat(money);
                }
            } else if (TypeCash.ON_SiTE.getTitle().equals(typeCash)) {
                if (!report.getPropertyAsString(moneys + 3).equals("anyType{}")) {
                    String money = report.getPropertyAsString(moneys + 3);
                    on_site += Float.parseFloat(money);
                }
            } else if (TypeCash.TRANSFER.getTitle().equals(typeCash)) {
                if (!report.getPropertyAsString(moneys + 3).equals("anyType{}")) {
                    String money = report.getPropertyAsString(moneys + 3);
                    transfer += Float.parseFloat(money);
                }
            } else if (TypeCash.ON_TERMINAL.getTitle().equals(typeCash)) {
                if (!report.getPropertyAsString(moneys + 3).equals("anyType{}")) {
                    String money = report.getPropertyAsString(moneys + 3);
                    on_terminal += Float.parseFloat(money);
                }
            }

            if (!report.getPropertyAsString(tank + 4).equals("anyType{}")) {
                container = report.getPropertyAsString(tank + 4);
            } else container = "";

            if (!report.getPropertyAsString(orderCount + 5).equals("anyType{}")) {
                order = report.getPropertyAsString(orderCount + 5);
            } else order = "";

            type += 9;
            moneys += 9;
            tank += 9;
            orderCount +=9;
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        String date = format.format(calendar.getTime());

        Report reportDay = new Report(date);
        reportDay.setCash(cashes);
        reportDay.setNon_cash(non_cash);
        reportDay.setOn_terminal(on_terminal);
        reportDay.setOn_site(on_site);
        reportDay.setTransfer(transfer);
        reportDay.setTank(Integer.parseInt(container));
        reportDay.setOrderCount(Integer.parseInt(order));

        return reportDay;
    }

}
