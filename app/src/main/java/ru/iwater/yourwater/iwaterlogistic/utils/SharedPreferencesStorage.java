package ru.iwater.yourwater.iwaterlogistic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import ru.iwater.yourwater.iwaterlogistic.domain.Report;
import ru.iwater.yourwater.iwaterlogistic.domain.ReportOrder;

public class SharedPreferencesStorage {
    public static final String STORAGE_NAME = "DriverData";

    private static SharedPreferences settings = null;
    private static SharedPreferences.Editor editor = null;
    private static Context context = null;

    //инициализация SharedPreferences
    public static void init( Context cntxt ){
        context = cntxt;
    }

    private static void init(){
        settings = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.apply();
    }

    //добавление поля в кэш
    public static void addProperty(String name, String value ){
        if( settings == null ){
            init();
        }
        editor.putString( name, value );
        editor.apply();
    }

    public static void addReport(String name, int orderId, float cash, String typeCash, int tank) {
        if( settings == null ){
            init();
        }
        editor.putInt(name + "id", orderId);
        editor.putFloat(name + "cash", cash);
        editor.putString(name + "typeCash", typeCash);
        editor.putInt(name + "tank", tank);
        editor.apply();
    }

    public static ReportOrder getReport(String name) {
        if( settings == null ){
            init();
        }
        int id = settings.getInt(name + "id", 0);
        float cash = settings.getFloat(name + "cash", 0);
        String typeCash = settings.getString(name + "typeCash", "");
        int tank = settings.getInt(name + "tank", 0);
        ReportOrder reportOrder = new ReportOrder(id, cash, TypeCash.valueOf(typeCash));
        reportOrder.setThank(tank);
        removeReport(name);
        return reportOrder;
    }

    public static void removeReport(String name){
        if( settings == null ){
            init();
        }
        editor.remove(name + "id");
        editor.remove(name + "cash");
        editor.remove(name + "typeCash");
        editor.remove(name + "tank");
        editor.apply();
    }

    //проверка наличия поля
    public static Boolean checkReportProperty(String name){
        if( settings == null ){
            init();
        }
        return settings.contains(name + "id");
    }

    //получение поля из кэша
    public static String getProperty(String name ){
        if( settings == null ){
            init();
        }
        return settings.getString( name, null );
    }

    //удаление поля
    public static void removeProperty( String name ){
        if( settings == null ){
            init();
        }
        editor.remove(name);
        editor.apply();
    }

    //очистка всего кэша
    public static void clearAll(){
        if( settings == null ){
            init();
        }
        editor.clear();
        editor.apply();
    }

    //проверка наличия поля
    public static Boolean checkProperty(String name ){
        if( settings == null ){
            init();
        }
        return settings.contains(name);
    }
}
