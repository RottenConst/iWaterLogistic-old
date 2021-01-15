package ru.iwater.yourwater.iwaterlogistic.ui.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;

import ru.iwater.yourwater.iwaterlogistic.utils.Diagonal;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class FragmentNotificationHistory extends Fragment {

    private LinearLayout history;//список уведомлений
    private Diagonal diagonal;//диагональ устройства
    private TextView noNotifications;//надпись Нет уведомлений

    public FragmentNotificationHistory() {
    }

    public static FragmentNotificationHistory newInstance() {
        FragmentNotificationHistory fragment = new FragmentNotificationHistory();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferencesStorage.init(getActivity().getApplicationContext());
        diagonal = new Diagonal();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(history!=null)//если есть родительский элемент
                if(history.getChildCount()!=0)//и количество элементов в нём > 0
                    history.removeAllViews();//то удаляем все элементы
            makeHistory();//отрисовываем разметку заново с учётом новых элементов
            //обновляем родительский элемент
            history.refreshDrawableState();
            history.invalidate();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_notification_history, container, false);

        history = (LinearLayout) v.findViewById(R.id.notificationHistory);
        noNotifications = (TextView) v.findViewById(R.id.textView29);

        //название страницы
        try {
            getActivity().setTitle(R.string.titleNotifications);
        } catch (Exception e) {
            Log.e("iWaterLogistic","Получено исключение",e);
        }
        makeHistory();
        getActivity().registerReceiver(broadcastReceiver,new IntentFilter("ru.yourwater.iwaterlogistic.UPDATE_NOTIFICATIONS"));

        return v;
    }

    private void makeHistory(){
        boolean flag=false,flag1=false,flag2=false;
        try {
            if (SharedPreferencesStorage.checkProperty(Helper.returnFormatedDate(0))) {
                if(!SharedPreferencesStorage.getProperty(Helper.returnFormatedDate(0)).equals("")) {
                    makeMarking(SharedPreferencesStorage.getProperty(Helper.returnFormatedDate(0)));
                } else {
                    flag = true;
                }
            } else {
                flag = true;
            }
            if(SharedPreferencesStorage.checkProperty(Helper.returnFormatedDate(-1))){
                if(!SharedPreferencesStorage.getProperty(Helper.returnFormatedDate(-1)).equals("")) {
                    makeMarking(SharedPreferencesStorage.getProperty(Helper.returnFormatedDate(-1)));
                }else {
                    flag1 = true;
                }
            }else {
                flag1 = true;
            }
            if(SharedPreferencesStorage.checkProperty(Helper.returnFormatedDate(-2))){
                if(!SharedPreferencesStorage.getProperty(Helper.returnFormatedDate(-2)).equals("")) {
                    makeMarking(SharedPreferencesStorage.getProperty(Helper.returnFormatedDate(-2)));
                }else {
                    flag1 = true;
                }
            }else {
                flag2 = true;
            }

            if(flag && flag1 && flag2)
                noNotifications.setVisibility(View.VISIBLE);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void makeMarking(String str) throws ParseException, JSONException {

        float scale = getResources().getDisplayMetrics().density;//разрешение экрана
        //размеры////////////////////////////////////////////////////////////////////////////
        float textSize = 0;
        int _50dp = 0;
        int _5dp = 0;
        int _10dp = 0;
        if(diagonal.returnDiagonal(getActivity())>=7) {
            textSize = 22;
            _50dp = (int) (70 * scale + 0.5f);
            _5dp = (int) (10 * scale + 0.5f);
            _10dp = (int) (20 * scale + 0.5f);
        }
        else {
            textSize = 14;
            _50dp = (int) (50 * scale + 0.5f);
            _5dp = (int) (5 * scale + 0.5f);
            _10dp = (int) (10 * scale + 0.5f);
        }

        JSONArray jsonArray = new JSONArray(str);
        for (int i = jsonArray.length()-1; i >= 0; i--) {

            //region строка уведомления
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, _50dp);
            rowParams.bottomMargin = _10dp;

            LinearLayout row = new LinearLayout(getActivity().getApplicationContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundResource(R.drawable.row_background);
            row.setLayoutParams(rowParams);
            //endregion

            //region текст уведомления
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.leftMargin = _5dp;
            textParams.gravity = Gravity.CENTER_VERTICAL;

            TextView text = new TextView(getActivity().getApplicationContext());
            text.setTextColor(Color.BLACK);
            text.setTextSize(textSize);
            text.setLayoutParams(textParams);
            text.setText(jsonArray.getJSONObject(i).getString("notification"));
            //endregion

            //region дата уведомления
            TextView date = new TextView(getActivity().getApplicationContext());
            date.setTextColor(getResources().getColor(R.color.gray));
            date.setTextSize(textSize);
            date.setLayoutParams(textParams);
            date.setText(jsonArray.getJSONObject(i).getString("date"));
            //endregion

            row.addView(text);
            history.addView(date);
            history.addView(row);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
    }
}
