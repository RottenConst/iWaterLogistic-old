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

public class DRname extends AsyncTask<Void, Void, SoapObject> {
    private final static String SOAP_ACTION_REPORT_INSERTS = "urn:info#DRname";
    private final static String METHOD_REPORT_INSERTS = "DRname";
    private final static String NAMESPACE_REPORT_INSERTS = "urn:info";

//    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";
    private final static String URL = "http://iwatercrm.ru/iwater_api/driver/server.php?wsdl";
//    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php";

    @Override
    protected SoapObject doInBackground(Void... voids) {
        Float test = 501.00F;
        SoapObject result = null;
        SoapObject request = new SoapObject(NAMESPACE_REPORT_INSERTS, METHOD_REPORT_INSERTS);
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        request.addProperty("payment_type", "НАЛ");
        request.addProperty("payment", "10023");
        request.addProperty("number_containers", test.toString());
        soapEnvelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        //добавление в заголовок информации о формате сжимаемых данных(gzip)
        List<HeaderProperty> headers = new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty = new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);
        try {
            httpTransport.call(SOAP_ACTION_REPORT_INSERTS, soapEnvelope, headers);
            result = (SoapObject) soapEnvelope.getResponse();
            return result;
        } catch (Exception e) {
            Log.e("IWater", "получено исключение");
        }

        return result;
    }
}
