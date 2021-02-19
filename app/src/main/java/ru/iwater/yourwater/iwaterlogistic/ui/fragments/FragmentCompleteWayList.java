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
import java.util.List;

import ru.iwater.yourwater.iwaterlogistic.domain.OldOrder;
import ru.iwater.yourwater.iwaterlogistic.remote.DriverWayBill;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Diagonal;
import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class FragmentCompleteWayList extends Fragment {
    private DriverWayBill wayBill;//путевой лист на определённую дату
    private SoapObject wayBillData;//данные этого путевого листа
    private String session="";//ключ сессии
    //массивы для содержания строки заказа
    private List<OldOrder> oldOrders;
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
        View v = inflater.inflate(R.layout.fragment_order, container, false);

        //region инициализация
        SharedPreferencesStorage.init(getContext());
        diagonal = new Diagonal();

        //endregion

        if(SharedPreferencesStorage.checkProperty("session"))
            session = SharedPreferencesStorage.getProperty("session");

        //region загрузка данных
        if(Check.checkInternet(getContext())) {
            if(Check.checkServer(getContext())) {
                try {
                    wayBill = new DriverWayBill(session);
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
        oldOrders = new ArrayList<>();

        String idOrder;
        String dateOrder;
        String periodOrder;
        String addressOrder;
        String statusOrder;

        if (wayBillData != null) {
            List<OldOrder> rawOldOrder = new ArrayList<>();
            int id = 0, date = 0, period = 0, address = 0, status = 0;
            for (int f = 0; f < wayBillData.getPropertyCount()/13; f++) {
                if (!wayBillData.getPropertyAsString(id).equals("anyType{}"))
                    idOrder = wayBillData.getPropertyAsString(id);
                else
                    idOrder = "";
                if (!wayBillData.getPropertyAsString(date + 8).equals("anyType{}"))
                    dateOrder = wayBillData.getPropertyAsString(date + 8).replaceAll("/+","\\.");
                else
                    dateOrder = "";
                if (!wayBillData.getPropertyAsString(period + 9).equals("anyType{}"))
                    periodOrder = wayBillData.getPropertyAsString(period + 9);
                else
                    periodOrder = "";
                if (!wayBillData.getPropertyAsString(address + 10).equals("anyType{}"))
                    addressOrder = wayBillData.getPropertyAsString(address + 10);
                else
                    addressOrder = "";
                if (!wayBillData.getPropertyAsString(status + 12).equals("anyType{}"))
                    statusOrder = wayBillData.getPropertyAsString(status + 12);
                else
                    statusOrder = "";

                rawOldOrder.add(new OldOrder(idOrder, dateOrder, periodOrder, addressOrder, statusOrder));

                id += 13;
                date += 13;
                period += 13;
                address += 13;
                status += 13;
            }
           oldOrders = duplicateOrder(rawOldOrder);
        }
        //endregion

        if(oldOrders != null) {
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
            for (int i=0; i<oldOrders.size(); i++) {
                if (oldOrders.get(i).getStatus().equals("0")) {

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
                    text.setText("№ " + oldOrders.get(i).getId()+", "+oldOrders.get(i).getDate()+", "+oldOrders.get(i).getPeriod()+", "+oldOrders.get(i).getAddress());
                    //endregion

                    activeRow.addView(text);

                    activeOrders.addView(activeRow);

                    a++;
                } else if (oldOrders.get(i).getStatus().equals("1")) {

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
                    textComp.setText("№ " + oldOrders.get(i).getId()+", "+oldOrders.get(i).getDate()+", "+oldOrders.get(i).getPeriod()+", "+oldOrders.get(i).getAddress());
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

    private List<OldOrder> duplicateOrder(List<OldOrder> oldOrders) {
        sortOrder(oldOrders);

        List<OldOrder> sortOrder = new ArrayList<>();
        for (int i = 0; i < oldOrders.size() - 1; i++) {
            if(!oldOrders.get(i).getId().equals(oldOrders.get(i+1).getId())) {
                sortOrder.add(oldOrders.get(i));
            }
        }
        sortOrder.add(oldOrders.get(oldOrders.size() - 1));

        return sortOrder;
    }

    private void sortOrder(List<OldOrder> orders) {
        boolean sorted = false;
        OldOrder order;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < orders.size() - 1; i++){
                if (Integer.parseInt(orders.get(i).getId()) > Integer.parseInt(orders.get(i +1).getId())){
                    order = new OldOrder(orders.get(i).getId(),
                            orders.get(i).getDate(),
                            orders.get(i).getPeriod(),
                            orders.get(i).getAddress(),
                            orders.get(i).getStatus());
                    orders.set(i, orders.get(i + 1));
                    orders.set(i + 1, order);
                    sorted = false;
                }
            }
        }
    }

}
