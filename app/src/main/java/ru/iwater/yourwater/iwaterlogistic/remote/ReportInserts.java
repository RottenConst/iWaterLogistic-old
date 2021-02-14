package ru.iwater.yourwater.iwaterlogistic.remote;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.se.omapi.Session;
import android.util.Log;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;
import ru.iwater.yourwater.iwaterlogistic.utils.TypeCash;

public class ReportInserts extends AsyncTask<Void, Void, SoapObject> {

    private final static String SOAP_ACTION_REPORT_INSERTS = "urn:info#reportInserts";
    private final static String METHOD_REPORT_INSERTS = "reportInserts";
    private final static String NAMESPACE_REPORT_INSERTS = "urn:info";

//    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";
//    private final static String URL = "http://iwatercrm.ru/iwater_api/driver/server.php?wsdl";
    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php";

    private String payment_type;
    private Float payment;
    private Integer number_containers;
    private Integer orders_delivered;
    private Float total_money;
    private String login;
    private String company;

    public ReportInserts(String payment_type, float payment, int number_containers, int orders_delivered, float total_money) {
        this.payment_type = payment_type;
        this.payment = payment;
        this.number_containers = number_containers;
        this.orders_delivered = orders_delivered;
        this.total_money = total_money;
        if (SharedPreferencesStorage.checkProperty("login")) {
            login = SharedPreferencesStorage.getProperty("login");
        }
        if (SharedPreferencesStorage.checkProperty("company")) {
            company = SharedPreferencesStorage.getProperty("company");
        }
    }

    @Override
    protected SoapObject doInBackground(Void... voids) {
        SoapObject request = new SoapObject(NAMESPACE_REPORT_INSERTS, METHOD_REPORT_INSERTS);
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        request.addProperty("login", login);
        request.addProperty("payment_type", payment_type);
        request.addProperty("payment", payment.toString());
        request.addProperty("number_containers", number_containers);
        request.addProperty("orders_delivered", orders_delivered);
        request.addProperty("total_money", total_money.toString());
        request.addProperty("company", company);
        soapEnvelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        //добавление в заголовок информации о формате сжимаемых данных(gzip)
        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);
        try {
            httpTransport.call(SOAP_ACTION_REPORT_INSERTS, soapEnvelope, headers);
            SoapObject result = (SoapObject) soapEnvelope.getResponse();
            return result;
        }catch (Exception e) {
            Log.e("IWater", "получено исключение");
        }

        return null;
    }
}
