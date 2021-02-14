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
 * Класс для загрузки истории путевых листов
 */
public  class WayBillHistory extends AsyncTask<Void, Void, SoapObject> {
    private final static String SOAP_ACTION_WAYBIlLHISTORY = "urn:info#hist";
    private final static String METHOD_NAME_WAYBIlLHISTORY = "history";
    private final static String NAMESPACE_WAYBIlLHISTORY= "urn:info";

//    private final static String URL = "http://iwatercrm.ru/iwater_api/driver/server.php?wsdl";
    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php";
//    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    private String session = "";

    public WayBillHistory(String session){
        this.session = session;
    }

    @Override
    protected SoapObject doInBackground(Void... params) {

        SoapObject Request = new SoapObject(NAMESPACE_WAYBIlLHISTORY,METHOD_NAME_WAYBIlLHISTORY);

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.setOutputSoapObject(Request);
        Request.addProperty("session", session);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);

        try{
            httpTransport.call(SOAP_ACTION_WAYBIlLHISTORY,soapEnvelope,headers);
            SoapObject resultString = (SoapObject) soapEnvelope.getResponse();
            SoapObject result = (SoapObject) resultString.getProperty(0);
            Log.d("FragmentWayLists", "count: " + String.valueOf(result.getPropertyCount()));
            return result;
        }catch (Exception e) {
            Log.e("iWater","Получено исключение",e);
        }
        return null;
    }
}
