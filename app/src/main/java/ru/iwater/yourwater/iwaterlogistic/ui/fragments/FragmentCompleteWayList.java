package ru.iwater.yourwater.iwaterlogistic.ui.fragments;


import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Diagonal;
import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;
import ru.iwater.yourwater.iwaterlogistic.remote.WayBill;

public class FragmentCompleteWayList extends Fragment {
    private WayBill wayBill;//путевой лист на определённую дату
    private SoapObject wayBillData;//данные этого путевого листа
    private String session="";//ключ сессии
    //массивы для содержания строки заказа
    private ArrayList<String> ID;
    private ArrayList<String> DATE;
    private ArrayList<String> PERIOD;
    private ArrayList<String> ADDRESS;
    private ArrayList<String> STATUS;
    private LinearLayout activeOrders;//незавершённые заказы
    private LinearLayout complitOrders;//завершённые заказы
    //надписи в случае отсутствия заказов
    private TextView activeOrdersAreAbsent;
    private TextView completeOrdersAreAbsent;
    private Diagonal diagonal;//диагональ устройства
    private String id="";//номер путевого листа

    public FragmentCompleteWayList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            id = bundle.getString("waybill_id");
            //position = bundle.getInt("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_complete_way_list, container, false);

        //region инициализация
        SharedPreferencesStorage.init(getContext());
        diagonal = new Diagonal();

        activeOrders = (LinearLayout) v.findViewById(R.id.currentOrd);
        complitOrders = (LinearLayout) v.findViewById(R.id.completeOrd);
        activeOrdersAreAbsent = (TextView) v.findViewById(R.id.textView23);
        completeOrdersAreAbsent = (TextView) v.findViewById(R.id.textView24);
        //endregion

        if(SharedPreferencesStorage.checkProperty("session"))
            session = SharedPreferencesStorage.getProperty("session");

        //region загрузка данных
        if(Check.checkInternet(getContext())) {
            if(Check.checkServer(getContext())) {
                try {
                    wayBill = new WayBill(session, id);
                    wayBill.execute();
                    wayBillData = wayBill.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }else{
                Toast toast = Toast.makeText(getContext(),R.string.CheckServer, Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getContext(),R.string.CheckInternet, Toast.LENGTH_LONG);
            toast.show();
        }
        //endregion
        //region заполнение массивов
        ID = new ArrayList<String>();
        DATE = new ArrayList<String>();
        PERIOD = new ArrayList<String>();
        ADDRESS = new ArrayList<String>();
        STATUS = new ArrayList<String>();

        if (wayBillData != null) {
            int i=0,j=0,k=0,l=0,h=0;
            for (int f = 0; f < wayBillData.getPropertyCount()/5; f++) {
                if (!wayBillData.getPropertyAsString(i).equals("anyType{}"))
                    ID.add(wayBillData.getPropertyAsString(i));
                else
                    ID.add("");
                if (!wayBillData.getPropertyAsString(j + 1).equals("anyType{}"))
                    DATE.add(wayBillData.getPropertyAsString(j + 1).replaceAll("/+","\\."));
                else
                    DATE.add("");
                if (!wayBillData.getPropertyAsString(k + 2).equals("anyType{}"))
                    PERIOD.add(wayBillData.getPropertyAsString(k + 2));
                else
                    PERIOD.add("");
                if (!wayBillData.getPropertyAsString(l + 3).equals("anyType{}"))
                    ADDRESS.add(wayBillData.getPropertyAsString(l + 3));
                else
                    ADDRESS.add("");
                if (!wayBillData.getPropertyAsString(h + 4).equals("anyType{}"))
                    STATUS.add(wayBillData.getPropertyAsString(h + 4));
                else
                    STATUS.add("");
                i += 5;
                j += 5;
                k += 5;
                l += 5;
                h += 5;
            }
        }
        //endregion

        if(ID != null) {
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

            int c=0,a=0;//счётчики
            for (int i=0; i<ID.size(); i++) {
                if (STATUS.get(i).equals("0")) {

                    //region строка незавершённого заказа
                    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, _50dp);
                    rowParams.bottomMargin = _10dp;

                    LinearLayout activeRow = new LinearLayout(getContext());
                    activeRow.setOrientation(LinearLayout.HORIZONTAL);
                    activeRow.setBackgroundResource(R.drawable.press_blue_row);
                    activeRow.setLayoutParams(rowParams);
                    activeRow.setId(i);
                    //endregion

                    //region заказ
                    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    textParams.leftMargin = _5dp;
                    textParams.weight = 1;
                    textParams.gravity = Gravity.CENTER_VERTICAL;

                    TextView text = new TextView(getContext());
                    text.setTextSize(textSize);
                    text.setTextColor(Color.BLACK);
                    text.setLayoutParams(textParams);
                    text.setMaxLines(1);
                    text.setText("№ " + ID.get(i)+", "+DATE.get(i)+", "+PERIOD.get(i)+", "+ADDRESS.get(i));
                    //endregion

                    activeRow.addView(text);

                    activeOrders.addView(activeRow);

                    a++;
                } else if (STATUS.get(i).equals("1")) {

                    //region строка завершённого заказа
                    LinearLayout.LayoutParams rowCompParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, _50dp);
                    rowCompParams.bottomMargin = _10dp;

                    LinearLayout complitRow = new LinearLayout(getContext());
                    complitRow.setOrientation(LinearLayout.HORIZONTAL);
                    complitRow.setBackgroundResource(R.drawable.row_background);
                    complitRow.setLayoutParams(rowCompParams);
                    //endregion

                    //region заказ
                    LinearLayout.LayoutParams textCompParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    textCompParams.leftMargin = _5dp;
                    textCompParams.weight = 1;
                    textCompParams.gravity = Gravity.CENTER_VERTICAL;

                    TextView textComp = new TextView(getContext());
                    textComp.setTextSize(textSize);
                    textComp.setTextColor(Color.BLACK);
                    textComp.setLayoutParams(textCompParams);
                    textComp.setMaxLines(1);
                    textComp.setText("№ " + ID.get(i)+", "+DATE.get(i)+", "+PERIOD.get(i)+", "+ADDRESS.get(i));
                    //endregion

                    complitRow.addView(textComp);
                    complitOrders.addView(complitRow);

                    c++;
                }
            }

            if(a == 0)
                activeOrdersAreAbsent.setVisibility(View.VISIBLE);
            if(c==0)
                completeOrdersAreAbsent.setVisibility(View.VISIBLE);
        }

        return v;
    }

}
