package ru.iwater.yourwater.iwaterlogistic.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketError;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;

import java.util.List;
import java.util.Map;

import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class LocationService extends Service {

    private String latitude = "", longitude = "";//широта и долгота
    private String coords = "";//координаты в формате 54.87484,76.92734
    private WebSocket ws;//веб сокет клиент
    private LocationManager mLocationManager = null;//класс, обеспечивающий доступ к геолокации


    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation.set(location);
            Log.d("LocationService", "onLocationChanged");
            if (location != null) {
                if (latitude.equals("") && longitude.equals("")) {
                    latitude = String.valueOf(location.getLatitude()).replaceAll(",", "\\.");
                    longitude = String.valueOf(location.getLongitude()).replaceAll(",", "\\.");
//                    Log.d("LocationService:", String.valueOf(location.getLatitude()) + ";" + String.valueOf(location.getLongitude()));
                }
            } else {
                Log.d("LocationService", "location = null");
            }

            if (!latitude.equals("") && !longitude.equals("")) {
                coords = latitude + "," + longitude;
                Log.d("LocationService", "coords = "+coords);
                latitude = "";
                longitude = "";
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    //инициализация LocationManager
    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onCreate() {
        SharedPreferencesStorage.init(getApplicationContext());
        initializeLocationManager();
        requestLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationService", "START LOCATIONSERVICE");

            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(15000);
            try {
                ws = factory.createSocket("ws://95.213.183.181:10030");
                ws.connectAsynchronously();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ws.addListener(new WebSocketListener() {
                @Override
                public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                    if (newState.compareTo(WebSocketState.CLOSED) == 0) {
                        if (SharedPreferencesStorage.checkProperty("session")) {
                            Log.d("LocationService", "WebSocket try connect...");
                            ws = websocket.recreate().connect();
                        }
                    }

                    if (newState.compareTo(WebSocketState.OPEN) == 0) {
                        Log.d("LocationService", "WebSocket connection is successfully");
                    }
                    Log.d("LocationService","state_changed" + newState.name());
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    Log.d("LocationService", "WebSocket connect");
                    Intent service = new Intent(getApplicationContext(), RestartLocationService.class);
                    getApplicationContext().stopService(service);
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.e("LocationService", "WebSocket connection Получено исключение", cause);
                    /*FileWriter writer = new FileWriter("C:\\Users\\Uher\\Desktop\\logs.txt", false);
                    writer.write(cause.getMessage());*/
                    if(cause.getError().compareTo(WebSocketError.SOCKET_CONNECT_ERROR)==0){
                        if (SharedPreferencesStorage.checkProperty("session")) {
                            Log.d("LocationService", "WebSocket try connect...");
                            ws = websocket.recreate().connect();
                        }
                    }
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    Log.d("LocationService", "WebSocket disconnect");
                    if(SharedPreferencesStorage.checkProperty("session")){
                        if(!Check.checkInternet(getApplicationContext())){
                            Intent service = new Intent(getApplicationContext(), LocationService.class);
                            getApplicationContext().stopService(service);
                            Intent service1 = new Intent(getApplicationContext(), RestartLocationService.class);
                            getApplicationContext().startService(service1);
                        }
                    }
                }

                @Override
                public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onFrame ***********************");
                }

                @Override
                public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onContinuationFrame ***********************");
                }

                @Override
                public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onTextFrame ***********************");
                }

                @Override
                public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onBinaryFrame ***********************");
                }

                @Override
                public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onCloseFrame ***********************");
                }

                @Override
                public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onPingFrame ***********************");
                }

                @Override
                public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onPongFrame ***********************");
                }

                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    String response = "";
                    Log.d("LocationService", "WebSocket " + text);
                    if(ws == null)
                        Log.d("LocationService","WebSocket ws=null");
                    if (text.replaceAll("\\s+", "").equals("connected")) {
                        if (SharedPreferencesStorage.checkProperty("id")) {
                            response = SharedPreferencesStorage.getProperty("id");
                            Log.d("LocationService", "WebSocket send id=" + response);
                        }
                        websocket.sendText(response);
                    }
                    if (text.replaceAll("\\s+", "").equals("ucoord?")) {
                        for(;;) {
                            if(mLocationManager!=null && !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                                break;
                            else if (coords.length()>3) {
                                ws.sendText(coords);
                                Log.d("LocationService", "WebSocket sending coords=" + coords);
                                break;
                            }

                        }
                    }
                }

                @Override
                public void onTextMessage(WebSocket websocket, byte[] data) throws Exception {

                }

                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                    Log.d("LocationService", "onBinaryMessage ***********************");
                }

                @Override
                public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onSendingFrame ***********************");
                }

                @Override
                public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onFrameSent ***********************");
                }

                @Override
                public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onFrameUnsent ***********************");
                }

                @Override
                public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                    Log.d("LocationService", "onThreadCreated ***********************");
                }

                @Override
                public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                    Log.d("LocationService", "onThreadStarted ***********************");
                }

                @Override
                public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                    Log.d("LocationService", "onThreadStopping ***********************");
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.d("LocationService", "onError ***********************");
                    /*FileWriter writer = new FileWriter("C:\\Users\\Uher\\Desktop\\logs.txt", false);
                    writer.write(cause.getMessage());*/
                }

                @Override
                public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onFrameError ***********************");
                }

                @Override
                public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
                    Log.d("LocationService", "onMessageError ***********************");
                }

                @Override
                public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
                    Log.d("LocationService", "onMessageDecompresError ***********************");
                }

                @Override
                public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
                    Log.d("LocationService", "onTextMessageError ***********************");
                }

                @Override
                public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                    Log.d("LocationService", "onSendError ***********************");
                }

                @Override
                public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.d("LocationService", "onUnexpectedError ***********************");
                }

                @Override
                public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
                    Log.d("LocationService", "handleCallbackError ***********************");
                }

                @Override
                public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
                    Log.d("LocationService", "onSendingHandshake ***********************");
                }
            });


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        Log.d("LocationService", "STOP LOCATIONSERVICE");
        if(ws!=null)
            ws.disconnect();
        if(mLocationManager!=null) {
            mLocationManager.removeUpdates(mLocationListeners[0]);
            mLocationManager.removeUpdates(mLocationListeners[1]);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //запрос местоположения
    private void requestLocation() {
//        Log.d("LocationService", "Se");
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0,mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

}
