package ru.iwater.yourwater.iwaterlogistic.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.domain.Account;
import ru.iwater.yourwater.iwaterlogistic.remote.Authorisation;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class LoginActivity extends AppCompatActivity {

    private Account account;
    private EditText companyIdEdit, loginEdit, passwordEdit;//поля с вводимыми данными
    private Button loginBTN;//кнопка Войти
    private Authorisation auth;//асинхронная авторизация
    private final String blockCharacterSet = " ";
    private LocationManager locationManager;
    //для запроса разрешений
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ru.iwater.yourwater.iwaterlogistic.R.layout.activity_main);

        SharedPreferencesStorage.init(getApplicationContext());
        account = new Account();

        //region определяем элементы по их id
        companyIdEdit = (EditText) findViewById(R.id.ID);
        loginEdit = (EditText) findViewById(R.id.login);
        passwordEdit = (EditText) findViewById(R.id.password);
        loginBTN = (Button) findViewById(R.id.loginBTN);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //endregion

        //region запрет ввода пробелов
        InputFilter filter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        loginEdit.setFilters(new InputFilter[]{filter});
        //endregion

        //вход
        inputAcc();

        final SharedPreferences sp = getSharedPreferences("ProtectedApps", MODE_PRIVATE);

        //if(SharedPreferencesStorage.checkProperty("autoRun") && SharedPreferencesStorage.getProperty("autoRun").equals("false")) {
        if ("xiaomi".equalsIgnoreCase(android.os.Build.MANUFACTURER) && !sp.getBoolean("protected", false)) {
            AlertDialog goToSettings = new AlertDialog.Builder(this)
                    .setMessage(R.string.goToSettings)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                            startActivity(intent);
                            sp.edit().putBoolean("protected", true).apply();
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create();

            goToSettings.show();
        }
        //}

        //if(SharedPreferencesStorage.checkProperty("autoRun") && SharedPreferencesStorage.getProperty("autoRun").equals("false")) {
        if ("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER) && !sp.getBoolean("protected", false)) {
            //final SharedPreferences.Editor editor = sp.edit();
            AlertDialog goToSettings = new AlertDialog.Builder(this)
                    .setMessage(R.string.goToSettings)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                            startActivity(intent);
                            sp.edit().putBoolean("protected", true).apply();
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create();

            goToSettings.show();
        }
        //}


        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    //вход
    private void inputAcc() {
        loginBTN.setOnClickListener(v -> {
            account.setCompany(companyIdEdit.getText().toString());
            account.setLogin(loginEdit.getText().toString());
            account.setPassword(passwordEdit.getText().toString());
            if (Check.checkInternet(getApplicationContext())) {
                if (Check.checkServer(getApplicationContext())) {
                    if (checkFields()) {
                        if (checkEnabled()) {
                            try {
                                if (SharedPreferencesStorage.checkProperty("token"))
                                    account.setToken(SharedPreferencesStorage.getProperty("token"));
                                else {
                                    FirebaseInstanceId.getInstance().getInstanceId()
                                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                    if (!task.isSuccessful()) {
                                                        Log.w("firebase", "getInstanceId failed", task.getException());
                                                    } else
                                                        account.setToken(task.getResult().getToken());
//                                                    Log.d("MAIN_ACTIVITY", account.getToken());
                                                }
                                            });
                                }
                                auth = new Authorisation(account, getApplicationContext());
                                auth.execute();
                            } catch (Exception e) {
                                Log.e("iWater Logistic", "Получено исключение", e);
                            }
                        }
                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.CheckServer, Toast.LENGTH_LONG);
                    toast.show();
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.CheckInternet, Toast.LENGTH_LONG);
                toast.show();
            }
        });
        //endregion
    }

    //проверка доступности Местоположения
    private boolean checkEnabled() {
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.checkGPS, Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
        return true;
    }

    //region метод проверки полей на пустоту
    private Boolean checkFields() {
        if (companyIdEdit.getText().toString().replaceAll("\\s+", "").length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.companyIdError, Toast.LENGTH_LONG);
            toast.show();
            return false;
        } else if (loginEdit.getText().toString().replaceAll("\\s+", "").length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.loginError, Toast.LENGTH_LONG);
            toast.show();
            return false;
        } else if (passwordEdit.getText().toString().replaceAll("\\s+", "").length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.passwordError, Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
        return true;
    }
    //endregion

    //ответ поьзователя на запрос разрешения функции местоположения
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_FINE_LOCATION) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                final AlertDialog explanation = new AlertDialog.Builder(this)
                        .setMessage(R.string.explanationGPS)
                        .setPositiveButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(LoginActivity.this,
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
    }
}


