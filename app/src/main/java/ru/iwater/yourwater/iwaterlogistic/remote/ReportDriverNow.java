package ru.iwater.yourwater.iwaterlogistic.remote;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class ReportDriverNow extends AsyncTask<Void, Void, SoapObject> {
    private final static String SOAP_ACTION_REPORT_NOW = "urn:info#reportDriverNow";
    private final static String METHOD_REPORT_NOW = "reportDriverNow";
    private final static String NAMESPACE_REPORT_NOW = "urn:info";

//    private final static String URL = "http://api.iwatercrm.ru/iwater_api/driver/server.php?wsdl";
    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php?wsdl";

    private String login;

    public ReportDriverNow() {
        if (SharedPreferencesStorage.checkProperty("login")) {
            login = SharedPreferencesStorage.getProperty("login");
        }
    }

    @Override
    protected SoapObject doInBackground(Void... voids) {
        SoapObject request = new SoapObject(NAMESPACE_REPORT_NOW, METHOD_REPORT_NOW);
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        request.addProperty("login", login);

        soapEnvelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        //добавление в заголовок информации о формате сжимаемых данных(gzip)
        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);
        try {
            httpTransport.call(SOAP_ACTION_REPORT_NOW, soapEnvelope, headers);
            SoapObject result = (SoapObject) soapEnvelope.getResponse();
            SoapObject report = (SoapObject) result.getProperty(0);
            return report;
        }catch (Exception e) {
            Log.e("IWater", "получено исключение");
        }
        return null;
    }
}
