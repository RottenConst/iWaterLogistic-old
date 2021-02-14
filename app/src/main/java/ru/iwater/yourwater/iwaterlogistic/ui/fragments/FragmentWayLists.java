package ru.iwater.yourwater.iwaterlogistic.ui.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Diagonal;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;
import ru.iwater.yourwater.iwaterlogistic.remote.WayBillHistory;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.CompleteWayList;

public class FragmentWayLists extends Fragment {

    //region ПЕРЕМЕННЫЕ
    private LinearLayout currentWayList;//строка Текущий путевой лист
    private LinearLayout complitWayLists;//завершённые путевые листы
    private String session="";//ключ сессии
    private WayBillHistory wayBillHistory;//загрузка истории путевых листов
    private SoapObject dateList;//результат загрузки
    private TextView currentDate;//текущая дата
    private TextView noWayBill;//надпись Нет путевых листов
    private BottomNavigationView bottomNavigation;//нижняя навигация
    private Diagonal diagonal;//диагональ устройства
    private ArrayList<String> IDs;//номера путевых листов
    private ArrayList<String> DATEs;//даты путевых листов
    //endregion

    public FragmentWayLists() {

    }

    public static FragmentWayLists newInstance() {
        FragmentWayLists fragment = new FragmentWayLists();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferencesStorage.init(getContext());
        diagonal = new Diagonal();
        IDs = new ArrayList<String>();
        DATEs = new ArrayList<String>();

        if(SharedPreferencesStorage.checkProperty("session"))
            session = SharedPreferencesStorage.getProperty("session");

        //region загрузка данных
        if (Check.checkInternet(getContext())) {
            if(Check.checkServer(getContext())) {
                try {
                    wayBillHistory = new WayBillHistory(session);
                    wayBillHistory.execute();
                    dateList = wayBillHistory.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }

        Log.d("FragmentWayLists", String.valueOf(dateList));

        //endregion

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_way_lists, container, false);

        currentWayList = v.findViewById(R.id.currentList);
        complitWayLists = v.findViewById(R.id.completeWayLists);
        currentDate = v.findViewById(R.id.textView6);
        if(getActivity() != null)
            bottomNavigation = getActivity().findViewById(R.id.bottomNavigation);
        noWayBill = v.findViewById(R.id.textView28);

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

        //переход к текущему путевому листу
        currentWayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().replace(R.id.Container, FragmentContainer.newInstance(0)).commit();
                }
                bottomNavigation.getMenu().getItem(1).setChecked(true);
            }
        });

        //название страницы
        try {
            getActivity().setTitle(R.string.titleWayLists);
        } catch (Exception e) {
            Log.e("iWaterLogistic","Получено исключение",e);
        }

        try {
            currentDate.setText(returnDate(String.valueOf(System.currentTimeMillis()/1000)));//ставим текущую дату
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(dateList!=null) {
            if(!dateList.toString().equals("anyType{}")) {
                /*int index=0;
                //проверка совпадения текущей даты с первым элементом в списке дат
                try {
                    if (currentDate.getText().toString().equals(returnDate(dateList.getPropertyAsString(1)))||Long.parseLong(dateList.getPropertyAsString(1))>(long)System.currentTimeMillis()/1000)
                        index = 2;
                    else
                        index = 0;
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/

                int g=0,k=0;
                if (dateList.getPropertyCount()==2 && dateList.getPropertyAsString(1).equals(Helper.returnUNIXDate())) {
                    noWayBill.setVisibility(View.VISIBLE);
                    Log.d("FragmentWayLists","noWayBills");
                } else {
                    int i = 0;
                    do{
//                            region строка с выполненными путевыми листами
                            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, _50dp);
                            rowParams.bottomMargin = _10dp;

                            LinearLayout row = new LinearLayout(getContext());
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setBackgroundResource(R.drawable.press_gray_row);
                            row.setLayoutParams(rowParams);
                            row.setId(i);

                            row.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (Check.checkInternet(getContext())) {
                                        Intent intent = new Intent(getContext(), CompleteWayList.class);
                                        if (!IDs.get(v.getId()).equals(""))
                                            intent.putExtra("id", IDs.get(v.getId()));
                                        try {
                                            if (!DATEs.get(v.getId()).equals(""))
                                                intent.putExtra("formated_date", returnDate(DATEs.get(v.getId())));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        startActivity(intent);
                                    } else {
                                        Toast toast = Toast.makeText(getContext(), R.string.CheckInternet, Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                }
                            });
//                            endregion
//
//                            region дата заказа
                            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            textParams.leftMargin = _5dp;
                            textParams.gravity = Gravity.CENTER_VERTICAL;

                            TextView date = new TextView(getContext());
                            date.setTextColor(Color.BLACK);
                            date.setTextSize(textSize);
                            date.setLayoutParams(textParams);
                            try {
                                if (!dateList.getPropertyAsString(g + 1).equals("anyType{}"))
                                    date.setText(returnDate(dateList.getPropertyAsString(g + 1)));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
//                            endregion
//
                            row.addView(date);
                            complitWayLists.addView(row);
//
                            if (!dateList.getPropertyAsString(k).equals("anyType{}"))
                                IDs.add(dateList.getPropertyAsString(k));
                            else
                                IDs.add("");
                            if (!dateList.getPropertyAsString(g + 1).equals("anyType{}"))
                                DATEs.add(dateList.getPropertyAsString(g + 1));
                            else
                                DATEs.add("");

                            g += 2;
                            k += 2;
                            i++;
                    }while ( i < dateList.getPropertyCount()/2);
                }
            }
        } else {
            noWayBill.setVisibility(View.VISIBLE);
        }

        return v;
    }

    //конвертация даты из юникс формата
    private String returnDate(String time) throws ParseException {
        long timeInMillis = Long.parseLong(time)*1000;

        /*TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");
        Calendar calendar = Calendar.getInstance(tz);*/
        Date date = new Date(timeInMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM");
        String formattedDate = sdf.format(date);

        return formattedDate;
    }

}
