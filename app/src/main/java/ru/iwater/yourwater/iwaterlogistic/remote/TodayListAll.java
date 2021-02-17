package ru.iwater.yourwater.iwaterlogistic.remote;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * класс для запроса и получения всех путех листов водителей
 */
public class TodayListAll extends AsyncTask<Void, Void, SoapObject> {

    private final static String SOAP_ACTION_TODAYLISTAll = "urn:info#td_list_All";
    private final static String METHOD_NAME_TODAYLISTALL = "td_list_All";
    private final static String NAMESPACE_TODAYLISTAll= "urn:info";

//    private final static String URL = "http://iwatercrm.ru/iwater_api/driver/server.php?wsdl";
//    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php";
    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    @Override
    protected SoapObject doInBackground(Void... voids) {
        SoapObject Request = new SoapObject(NAMESPACE_TODAYLISTAll, METHOD_NAME_TODAYLISTALL);

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.setOutputSoapObject(Request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        List<HeaderProperty> headers= new ArrayList<>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);
        try{
            httpTransport.call(SOAP_ACTION_TODAYLISTAll,soapEnvelope,headers);
            SoapObject res = (SoapObject) soapEnvelope.getResponse();
//            Log.d("TodayList", "Today Lists1 = " + res);

            return res;
        }catch (Exception e) {
            Log.e("iWater","Получено исключение",e);
        }
        return null;
    }
}
