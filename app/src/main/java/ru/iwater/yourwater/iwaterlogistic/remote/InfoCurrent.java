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
 * Класс для получения координат заказа по ID заказа
 */
public class InfoCurrent extends AsyncTask<Void, Void, SoapObject> {

    private final static String SOAP_ACTION_INFO_CURRENT = "urn:info#infoCurrent";
    private final static String METHOD_NAME_INFO_CURRENT = "infoCurrent";
    private final static String NAMESPACE_INFO_CURRENT= "urn:info";

//    private final static String URL = "http://iwatercrm.ru/iwater_api/driver/server.php?wsdl";
    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    private final String idOrder;

    public InfoCurrent(String idOrder) {
        this.idOrder = idOrder;
    }

    @Override
    protected SoapObject doInBackground(Void... voids) {
        SoapObject Request = new SoapObject(NAMESPACE_INFO_CURRENT,METHOD_NAME_INFO_CURRENT);

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.setOutputSoapObject(Request);
        Request.addProperty("id", idOrder);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);

        try{
            httpTransport.call(SOAP_ACTION_INFO_CURRENT,soapEnvelope,headers);
            SoapObject res = (SoapObject) soapEnvelope.getResponse();

            return res;
        }catch (Exception e) {
            Log.e("iWater","Получено исключение",e);
        }
        return null;
    }
}
