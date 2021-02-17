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

public class TypeClient extends AsyncTask<Void, Void, SoapObject> {

    private final static String SOAP_ACTION_TYPE_CLIENT = "urn:info#typeClient";
    private final static String METHOD_NAME_TYPE_CLIENT = "typeClient";
    private final static String NAMESPACE_TYPE_CLIENT= "urn:info";

//    private final static String URL = "http://iwatercrm.ru/iwater_api/driver/server.php?wsdl";
//    private final static String URL = "http://dev.iwatercrm.ru/iwater_logistic/driver/server.php";
    private final static String URL = "http://dev.iwatercrm.ru/iwater_api/driver/server.php?wsdl";

    private final String idOrders;

    public TypeClient(String idOrders) {
        this.idOrders = idOrders;
    }

    @Override
    protected SoapObject doInBackground(Void... voids) {
        SoapObject Request = new SoapObject(NAMESPACE_TYPE_CLIENT,METHOD_NAME_TYPE_CLIENT);

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.setOutputSoapObject(Request);
        Request.addProperty("id", idOrders);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        List<HeaderProperty> headers=new ArrayList<HeaderProperty>();
        HeaderProperty headerProperty=new HeaderProperty("Accept-Encoding", "none");
        headers.add(headerProperty);

        try{
            httpTransport.call(SOAP_ACTION_TYPE_CLIENT,soapEnvelope,headers);
            SoapObject res = (SoapObject) soapEnvelope.getResponse();

            return res;
        }catch (Exception e) {
            Log.e("iWater","Получено исключение",e);
        }
        return null;
    }
}
