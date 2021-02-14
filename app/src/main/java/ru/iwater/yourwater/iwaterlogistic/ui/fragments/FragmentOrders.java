package ru.iwater.yourwater.iwaterlogistic.ui.fragments;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.adapter.ListOrderAdapter;
import ru.iwater.yourwater.iwaterlogistic.domain.Order;
import ru.iwater.yourwater.iwaterlogistic.remote.DriverWayBill;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.IWaterActivity;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Diagonal;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class FragmentOrders extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    //region ПЕРЕМЕННЫЕ
    private static final String ARG_WAY_LIST = "waybill_id";
    private static final String ARG_POSITION = "position";
    private LinearLayout activeOrders;//текущие заказы
    private LinearLayout complitOrders;//завершённые заказы
    private LinearLayout performingOrders;//выполняющиеся заказы
    private TextView performText;//надпись Выполняются...
    private RecyclerView ordersList; //список заказов
    private RecyclerView completeList;
    private SoapObject ordersSoap;
    private JSONArray orderJson;
    private static ArrayList<Order> completeOrders;
    private static ArrayList<Order> orders; //заказы
    private ListOrderAdapter adapterListOrder;//адаптер для заказов
    private ListOrderAdapter completeAdapterListOrder;
    private String session = "";//ключ сессии
    private String id = "";//номер путевого листа

    //для разметки
    private boolean isRegistered = false;//проверка зарегистрирован приёмник или нет
    private int position = 0;//номер вкладки

    //endregion

    public FragmentOrders() {
    }

    public static FragmentOrders newInstance() {
        return new FragmentOrders();
    }

    public static FragmentOrders newInstance(String idWayList, int position) {
        FragmentOrders fragment = new FragmentOrders();
        Bundle arg = new Bundle();
        arg.putString(ARG_WAY_LIST, idWayList);
        arg.putInt(ARG_POSITION, position);
        fragment.setArguments(arg);
        return fragment;
    }

    //region обновление разметки при отгрузки заказа из очереди
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {

            if (activeOrders != null)
                if (activeOrders.getChildCount() != 0)
                    activeOrders.removeAllViews();

            if (performingOrders != null)
                if (performingOrders.getChildCount() != 0)
                    performingOrders.removeAllViews();

            if (complitOrders != null)
                if (complitOrders.getChildCount() != 0)
                    complitOrders.removeAllViews();

            if (!SharedPreferencesStorage.checkProperty("cashOrder" + position) && isRegistered) {
                context.unregisterReceiver(broadcastReceiver);
                isRegistered = false;
            }

            Log.d("msg", "я сработаль!");

            activeOrders.invalidate();
            activeOrders.refreshDrawableState();
            performingOrders.invalidate();
            performingOrders.refreshDrawableState();
            performText.invalidate();
            complitOrders.invalidate();
            complitOrders.refreshDrawableState();
        }
    };
    //endregion

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            id = bundle.getString("waybill_id");
            position = bundle.getInt("position");
        }
//        Log.d("FragmentOrders", "START FragmentOrders");

        SharedPreferencesStorage.init(getContext());

        if (SharedPreferencesStorage.checkProperty("waybill" + position)) {
            try {
                JSONArray jsonArray = new JSONArray(SharedPreferencesStorage.getProperty("waybill" + position));
                if (jsonArray.length() > 0) {
                    String wbDate = jsonArray.getJSONObject(0).getString("date").replaceAll("\\s+", "");
                    String curDate = returnDate();

                    if (!wbDate.equals(curDate))
                        SharedPreferencesStorage.removeProperty("waybill" + position);
//                    Log.d("***********************", "current=" + curDate + " waybill=" + wbDate);
                }
                for (int f = 0; f <= jsonArray.length(); f++) {
                    Log.d("FragmentOrders", jsonArray.get(f) + ", " + f);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (SharedPreferencesStorage.checkProperty("session")) {
            session = SharedPreferencesStorage.getProperty("session");
        }
        initData(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_order_test, container, false);

        ordersList = v.findViewById(R.id.list_order);
        completeList = v.findViewById(R.id.complite_orders);


        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        ordersList.setLayoutManager(llm);
        ordersList.setAdapter(adapterListOrder);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        completeList.setLayoutManager(linearLayoutManager);
        completeList.setAdapter(completeAdapterListOrder);

        if (SharedPreferencesStorage.checkProperty("cashOrder" + position) && !isRegistered) {
            if (getActivity() != null) {
                getActivity().registerReceiver(broadcastReceiver, new IntentFilter("ru.yourwater.iwaterlogistic.UPDATE_MARKING" + position));
                isRegistered = true;
            }
        }

        return v;
    }

    //region загрузка данных
    private SoapObject loadOrders(Context context) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
                    //загрузка путевого листа
                    DriverWayBill driverWayBill = new DriverWayBill(session, id);
                    driverWayBill.execute();
//                    Log.d("ORDER", driverWayBill.get().toString());
                    return driverWayBill.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
        return null;
    }

    @SuppressLint("NewApi")
    private void initData(Context context) {
        //заказы
        ordersSoap = loadOrders(context);
//        Log.d("FragmentOrders", "SOAP ORDERS + " + ordersSoap);
        //endregion
        if (ordersSoap != null) {
            orderJson = soapToJSON(ordersSoap);
//            Log.d("FragmentOrder", "Order = " + orderJson);
            if (SharedPreferencesStorage.checkProperty("waybill" + position)) {
                SharedPreferencesStorage.removeProperty("waybill" + position);
            }
            SharedPreferencesStorage.addProperty("waybill" + position, orderJson.toString());
            try {
                orders = initOrders(orderJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else orders = new ArrayList<>();
        //region кэширование путевого листа

        List<Order> activeOrder = parseOrder(orders);

        adapterListOrder = new ListOrderAdapter(getContext(), activeOrder);
        completeAdapterListOrder = new ListOrderAdapter(getContext(), completeOrders);
    }

    private JSONArray soapToJSON(SoapObject ordersSoap) {
        JSONArray jsonArrayOrder = new JSONArray();
        int id = 0, name = 0, order = 0, cash = 0, cash_b = 0, time = 0, contact = 0, notice = 0, date = 0, period = 0, address = 0, coords = 0, status = 0;
        for (int f = 0; f < ordersSoap.getPropertyCount() / 13; f++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("num", String.valueOf(f + 1));
                if (!ordersSoap.getPropertyAsString(id).equals("anyType{}"))
                    jsonObject.put("id", ordersSoap.getPropertyAsString(id));
                else
                    jsonObject.put("id", "");

                if (!ordersSoap.getPropertyAsString(name + 1).equals("anyType{}"))
                    jsonObject.put("name", ordersSoap.getPropertyAsString(name + 1));
                else
                    jsonObject.put("name", "");

                if (!ordersSoap.getPropertyAsString(order + 2).equals("anyType{}"))
                    jsonObject.put("order", ordersSoap.getPropertyAsString(order + 2));
                else
                    jsonObject.put("order", "");

                if (!ordersSoap.getPropertyAsString(cash + 3).equals("anyType{}"))
                    jsonObject.put("cash", ordersSoap.getPropertyAsString(cash + 3));
                else
                    jsonObject.put("cash", "0.00");

                if (!ordersSoap.getPropertyAsString(cash_b + 4).equals("anyType{}"))
                    jsonObject.put("cash_b", ordersSoap.getPropertyAsString(cash_b + 4));
                else
                    jsonObject.put("cash_b", "0.00");

                if (!ordersSoap.getPropertyAsString(time + 5).equals("anyType{}"))
                    jsonObject.put("time", ordersSoap.getPropertyAsString(time + 5));
                else
                    jsonObject.put("time", "");

                if (!ordersSoap.getPropertyAsString(contact + 6).equals("anyType{}"))
                    jsonObject.put("contact", ordersSoap.getPropertyAsString(contact + 6));
                else
                    jsonObject.put("contact", "");

                if (!ordersSoap.getPropertyAsString(notice + 7).equals("anyType{}"))
                    jsonObject.put("notice", ordersSoap.getPropertyAsString(notice + 7));
                else
                    jsonObject.put("notice", "");

                if (!ordersSoap.getPropertyAsString(date + 8).equals("anyType{}"))
                    jsonObject.put("date", ordersSoap.getPropertyAsString(date + 8).replaceAll("/+", "\\."));
                else
                    jsonObject.put("date", "");

                if (!ordersSoap.getPropertyAsString(period + 9).equals("anyType{}"))
                    jsonObject.put("period", ordersSoap.getPropertyAsString(period + 9));
                else
                    jsonObject.put("period", "");

                if (!ordersSoap.getPropertyAsString(address + 10).equals("anyType{}"))
                    jsonObject.put("address", ordersSoap.getPropertyAsString(address + 10));
                else
                    jsonObject.put("address", "");

                if (!ordersSoap.getPropertyAsString(coords + 11).equals("anyType{}"))
                    jsonObject.put("coords", ordersSoap.getPropertyAsString(coords + 11));
                else
                    jsonObject.put("coords", "");

                if (!ordersSoap.getPropertyAsString(status + 12).equals("anyType{}"))
                    jsonObject.put("status", ordersSoap.getPropertyAsString(status + 12));
                else
                    jsonObject.put("status", "");

                jsonArrayOrder.put(jsonObject);

                id += 13;
                name += 13;
                order += 13;
                cash += 13;
                cash_b += 13;
                time += 13;
                contact += 13;
                notice += 13;
                date += 13;
                period += 13;
                address += 13;
                coords += 13;
                status += 13;
            } catch (Exception e) {
                Log.e("iWaterlogistic/json", "Получено исключение", e);
            }

        }

        return jsonArrayOrder;

    }

    private List<Order> parseOrder(ArrayList<Order> orders) {
        completeOrders = new ArrayList<>();
        List<Order> activeOrder = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getStatus().equals("1")) {
                completeOrders.add(orders.get(i));
            } else {
                activeOrder.add(orders.get(i));
            }
        }
        return activeOrder;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<Order> initOrders(JSONArray waybill) throws JSONException {
        final ArrayList<Order> orders = new ArrayList<Order>();
        for (int i = 0; i < waybill.length(); i++) {
            orders.add(new Order(
                    waybill.getJSONObject(i).getString("id"),
                    waybill.getJSONObject(i).getString("name"),
                    waybill.getJSONObject(i).getString("order"),
                    waybill.getJSONObject(i).getString("cash"),
                    waybill.getJSONObject(i).getString("cash_b"),
                    waybill.getJSONObject(i).getString("time"),
                    waybill.getJSONObject(i).getString("contact"),
                    waybill.getJSONObject(i).getString("notice"),
                    waybill.getJSONObject(i).getString("date"),
                    waybill.getJSONObject(i).getString("period"),
                    waybill.getJSONObject(i).getString("address"),
                    waybill.getJSONObject(i).getString("status"),
                    waybill.getJSONObject(i).getString("coords")
            ));
        }
        //сортировка заказов
//        orders.sort(new Comparator<Order>() {
//            @Override
//            public int compare(Order o1, Order o2) {
//                return o1.getId().compareTo(o2.getId());
//            }
//        });
        sortOrder(orders);
        //удаление заказов
        ArrayList<Order> sortOrder = new ArrayList<>();
        for (int i = 0; i < orders.size() - 1; i++) {
            if(!orders.get(i).getId().equals(orders.get(i+1).getId())) {
                sortOrder.add(orders.get(i));
            }
        }
        sortOrder.add(orders.get(orders.size() - 1));

        return sortOrder;
    }

    private void sortOrder(ArrayList<Order> orders) {
        boolean sorted = false;
        Order order;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < orders.size() - 1; i++){
                if (Integer.parseInt(orders.get(i).getId()) > Integer.parseInt(orders.get(i +1).getId())){
                    order = new Order(orders.get(i).getId(),
                            orders.get(i).getName(),
                            orders.get(i).getOrder(),
                            orders.get(i).getCash(),
                            orders.get(i).getCash_b(),
                            orders.get(i).getTime(),
                            orders.get(i).getContact(),
                            orders.get(i).getNotice(),
                            orders.get(i).getDate(),
                            orders.get(i).getPeriod(),
                            orders.get(i).getAddress(),
                            orders.get(i).getStatus(),
                            orders.get(i).getCoords());
                    orders.set(i, orders.get(i + 1));
                    orders.set(i + 1, order);
                    sorted = false;
                }
            }
        }
    }


    //дата в формате 01.01.2018
    private String returnDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRegistered) {
            if (getActivity() != null) {
                getActivity().unregisterReceiver(broadcastReceiver);
                isRegistered = false;
            }
        }
    }

    @Override
    public void onRefresh() {
        initData(getContext());
//        refreshLayout.setRefreshing(false);
    }
}
