package ru.iwater.yourwater.iwaterlogistic.remote;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для получния путевых листов водителя по сессии(если сессии одинаковые у разных водителей вернуться
 * путевые листы разных водителей)
 */
public class TodayList extends AsyncTask<Void, Void, SoapObject> {

    private final static String SOAP_ACTION_TODAYLIST = "urn:info#today";
    private final static String METHOD_NAME_TODAYLIST = "todaylist";
    private final static String NAMESPACE_TODAYLIST= "urn:info";

    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php?wsdl";
//    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    private String session="";

    public TodayList(String session){
        this.session = session;
    }

    @Override
    protected SoapObject doInBackground(Void... voids) {
        SoapObject Request = new SoapObject(NAMESPACE_TODAYLIST,METHOD_NAME_TODAYLIST);

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.setOutputSoapObject(Request);
        Request.addProperty("session", session);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);

        try{
            httpTransport.call(SOAP_ACTION_TODAYLIST,soapEnvelope,headers);
            SoapObject res = (SoapObject) soapEnvelope.getResponse();
//            Log.d("TodayList",res.getPropertyAsString("id"));

            return res;
        }catch (Exception e) {
            Log.e("iWater","Получено исключение",e);
        }
        return null;
    }
}
