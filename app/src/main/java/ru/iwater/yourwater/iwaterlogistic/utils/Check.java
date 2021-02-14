package ru.iwater.yourwater.iwaterlogistic.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Check {

    // метод для проверки подключения////////////////////////////////////////////////////////
    public static boolean checkInternet(Context context) {

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int responseCode = 0;
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        // проверка подключения
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                CodeInternet code = new CodeInternet();
                code.execute();
                responseCode = code.get();
                if (responseCode == 200)
                    return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // метод для проверки работоспособности сервера////////////////////////////////////////////////////////
    public static boolean checkServer(Context context) {

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int responseCode = 0;
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        // проверка подключения
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                Code code = new Code();
                code.execute();
                responseCode = code.get();
                Log.d("*************response",String.valueOf(responseCode));
                if (responseCode == 200)
                    return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class Code extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                URL url = new URL("http://iwatercrm.ru/iwatercrm/iwater_api/driver/server.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                return urlConnection.getResponseCode();
            } catch (MalformedURLException e) {
                Log.e("checkInternet","Получено исключение",e);
            } catch (IOException e) {
                Log.e("checkInternet","Получено исключение",e);
            }
            return 0;
        }
    }

    public static class CodeInternet extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                URL url = new URL("https://www.google.com");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                return urlConnection.getResponseCode();
            } catch (MalformedURLException e) {
                Log.e("checkInternet","Получено исключение",e);
            } catch (IOException e) {
                Log.e("checkInternet","Получено исключение",e);
            }
            return 0;
        }
    }
}
