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

public class WayBill extends AsyncTask<Void, Void, SoapObject> {

    private final static String SOAP_ACTION_WAYBIlL = "urn:info#list";
    private final static String METHOD_NAME_WAYBIlL = "waybill";
    private final static String NAMESPACE_WAYBIlL= "urn:info";

//    private final static String URL = "http://iwatercrm.ru/iwater_api/driver/server.php?wsdl";
    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php";
//    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    private String session="";
    private String id="";

    public WayBill(String sess, String id){
        session = sess;
        this.id = id;
    }

    @Override
    protected SoapObject doInBackground(Void... params) {

        SoapObject Request = new SoapObject(NAMESPACE_WAYBIlL,METHOD_NAME_WAYBIlL);

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
            httpTransport.call(SOAP_ACTION_WAYBIlL,soapEnvelope,headers);//обращение к сервису
            SoapObject resultString = (SoapObject) soapEnvelope.getResponse();//получение ответа
            SoapObject result = (SoapObject) resultString.getProperty(0);
            return result;
        }catch (Exception e) {
            Log.e("iWater","Получено исключение",e);
        }
        return null;
    }

}
