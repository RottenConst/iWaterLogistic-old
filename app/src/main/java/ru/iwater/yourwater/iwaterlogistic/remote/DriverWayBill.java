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
 * Класс по сессии водителя и id путевого листа можно получить всю информацию о заказах путевого листа
 */
public class DriverWayBill extends AsyncTask<Void, Void, SoapObject> {

    private final static String SOAP_ACTION_DRIVER_WAY_BIlL = "urn:info#testlist";
    private final static String METHOD_NAME_DRIVER_WAY_BIlL = "testwaybill";
    private final static String NAMESPACE_DRIVER_WAY_BIlL= "urn:info";

//    private final static String URL = "http://iwatercrm.ru/iwater_api/driver/server.php?wsdl";
    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    private String session="";
    private String id="";

    public DriverWayBill(String session, String id){
        this.session = session;
        this.id = id;
    }

    @Override
    protected SoapObject doInBackground(Void... params) {
        Log.d("Order load", "id = " + id + " session = " + session);
        SoapObject Request = new SoapObject(NAMESPACE_DRIVER_WAY_BIlL,METHOD_NAME_DRIVER_WAY_BIlL);

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.setOutputSoapObject(Request);

        Request.addProperty("session", session);//добавление свойства с именем session в запрос
        Request.addProperty("id", id);//добавление свойства с именем id в запрос
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        //добавление в заголовок информации о формате сжимаемых данных(gzip)
        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);

        try{
            httpTransport.call(SOAP_ACTION_DRIVER_WAY_BIlL,soapEnvelope,headers);//обращение к сервису
            SoapObject resultString = (SoapObject) soapEnvelope.getResponse();//получение ответа
            SoapObject result = (SoapObject) resultString.getProperty(0);
            return result;
        }catch (Exception e) {
            Log.e("iWater","Получено исключение",e);
        }
        return null;
    }
}
