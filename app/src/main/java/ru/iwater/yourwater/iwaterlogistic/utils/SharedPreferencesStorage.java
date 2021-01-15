package ru.iwater.yourwater.iwaterlogistic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import ru.iwater.yourwater.iwaterlogistic.domain.Order;

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
