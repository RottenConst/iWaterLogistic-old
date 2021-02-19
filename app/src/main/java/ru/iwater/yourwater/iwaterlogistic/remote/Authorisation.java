package ru.iwater.yourwater.iwaterlogistic.remote;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.Services.LocationService;
import ru.iwater.yourwater.iwaterlogistic.Services.TimeListenerService;
import ru.iwater.yourwater.iwaterlogistic.domain.Account;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.IWaterActivity;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

/**
 * Класс для авторизации водителя
 */
public class Authorisation extends AsyncTask<Void, Void, String> {
    private final static String SOAP_ACTION_AUTH = "urn:authuser#auth";
    private final static String METHOD_NAME_AUTH = "auth";
    private final static String NAMESPACE_AUTH= "urn:authuser";

    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php?wsdl";
//    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    private Account account;
    private Context context;
    private String[] error;

    public Authorisation(Account account, Context context) {
        this.account = account;
        this.context = context;
    }



    @Override
    protected String doInBackground(Void... params) {

        SoapObject Request = new SoapObject(NAMESPACE_AUTH,METHOD_NAME_AUTH);

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        Request.addProperty("company", account.getCompany());
        Request.addProperty("login", account.getLogin());
        Request.addProperty("password", account.getPassword());
        Request.addProperty("notification", Helper.returnFormatedDate(0));
        soapEnvelope.setOutputSoapObject(Request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);

        try{
            httpTransport.call(SOAP_ACTION_AUTH,soapEnvelope,headers);
            String resultString = soapEnvelope.getResponse().toString();
            //парсим ответ сервиса, если ошибки нет возвращаем ID
            error = resultString.split(",");
            if (error[0].replaceAll("\\D+", "").equals("0")) {
                account.setId(error[1].replaceAll("]+", ""));
            } else if (error[0].replaceAll("\\D+", "").equals("1")) {
                account.setId(error[1].replaceAll("\\D+", ""));
            }
        }catch (Exception e) {
            Log.e("iWater","Получено исключение",e);
        }
//            Log.d("MAIN_ACTIVITY", ID);
        return account.getId();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(error!=null) {
            if (error[0].replaceAll("\\D+", "").equals("0")) {
                String[] arr = result.split("</session>");

                SharedPreferencesStorage.addProperty("session", arr[0].replaceAll("\\s+|<session>", ""));
                SharedPreferencesStorage.addProperty("login", account.getLogin());
                SharedPreferencesStorage.addProperty("company", account.getCompany());
                SharedPreferencesStorage.addProperty("id", arr[1].replaceAll("\\s+|<id>|</id>", ""));
                try {
                    SharedPreferencesStorage.addProperty(Helper.returnFormatedDate(0), "");
                    SharedPreferencesStorage.addProperty(Helper.returnFormatedDate(1), "");
                    SharedPreferencesStorage.addProperty(Helper.returnFormatedDate(2), "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //если регистрация прошла успешно, то запускаем стартовую страницу///////////////////////////////////
                Intent intent = new Intent(context, IWaterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);


                Intent service1 = new Intent(context.getApplicationContext(), LocationService.class);
                context.startService(service1);

                SharedPreferencesStorage.addProperty("k", "0");//коэффициент, который будет накапливаться при поступлении пуш уведомлений
            } else if (error[0].replaceAll("\\D+", "").equals("1")) {
                if (error[1].replaceAll("]+", "").equals(" Error: Input data not correct. Please try again!")) {
                    Toast toast = Toast.makeText(context.getApplicationContext(), R.string.dataNotCorrect, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(context.getApplicationContext(), R.string.unknownError, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }

}
