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

/**
 * Класс для получение путевых листов по id водетеля
 */
public class DriverTodayList extends AsyncTask<Void, Void, SoapObject> {
    private final static String SOAP_ACTION_DRIVER_TODAY_LIST = "urn:info#drivertodaylist";
    private final static String METHOD_NAME_DRIVER_TODAY_LIST = "drivertodaylist";
    private final static String NAMESPACE_DRIVER_TODAY_LIST= "urn:info";

    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php?wsdl";
//    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    private String driverId;

    public DriverTodayList(String driverId) {
        this.driverId = driverId;
    }

    @Override
    protected SoapObject doInBackground(Void... voids) {
        SoapObject request = new SoapObject(METHOD_NAME_DRIVER_TODAY_LIST, NAMESPACE_DRIVER_TODAY_LIST);
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        soapEnvelope.setOutputSoapObject(request);
        request.addProperty("id", driverId);
        HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);

        try{
            httpTransportSE.call(SOAP_ACTION_DRIVER_TODAY_LIST,soapEnvelope,headers);
            SoapObject res = (SoapObject) soapEnvelope.getResponse();
//            Log.d("TodayList",res.getPropertyAsString("id"));

            return res;
        }catch (Exception e) {
            Log.e("iWaterLoadDriver", "Error!");
        }
        return null;
    }
}
