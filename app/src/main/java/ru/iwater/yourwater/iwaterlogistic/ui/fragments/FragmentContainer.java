package ru.iwater.yourwater.iwaterlogistic.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.Arrays;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.domain.Account;
import ru.iwater.yourwater.iwaterlogistic.remote.DriverTodayList;
import ru.iwater.yourwater.iwaterlogistic.remote.DriverWayBill;
import ru.iwater.yourwater.iwaterlogistic.adapter.ViewPagerAdapterTab;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.map.GeneralMap;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class FragmentContainer extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private ViewPager2 container;//контейнер для вкладок
    private TabLayout wayLists;
    private SwipeRefreshLayout refreshLayout;
    private TextView generalMap;
    String[] todayWayListIds;//номера путевых листов
    private TextView noWayBills;//надпись нет путевых листов
    private SoapObject soapWayListIds;
    private int position = 0;
    private static final Account account = new Account();

    public static FragmentContainer newInstance(int position) {
        FragmentContainer fragment = new FragmentContainer();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt("position");
        }

        SharedPreferencesStorage.init(getContext());

        if (SharedPreferencesStorage.checkProperty("session")) {
            account.setSession(SharedPreferencesStorage.getProperty("session"));
        }
        if (SharedPreferencesStorage.checkProperty("id")) {
            account.setId(SharedPreferencesStorage.getProperty("id"));
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_container, container, false);

        //название страницы
        if (getActivity() != null)
            getActivity().setTitle(R.string.titleOrders);

        //вкладки
        wayLists = v.findViewById(R.id.tabs);
        this.container = v.findViewById(R.id.view_pager_container);
        noWayBills = v.findViewById(R.id.no_way_list_text);
        generalMap = v.findViewById(R.id.geniral_map_tv);

        refreshLayout = v.findViewById(R.id.refresh_test_container);

        refreshLayout.setOnRefreshListener(this);

        generalMap.setOnClickListener(v1 -> getGeneralMap());

        soapWayListIds = loadTodayList(getContext());
        todayWayListIds = parseSoapTodayListId(soapWayListIds);
        this.container.setCurrentItem(position);

        int i = -1;
        while (SharedPreferencesStorage.checkProperty("amountOfLists" + Helper.returnFormatedDate(i))) {
            SharedPreferencesStorage.removeProperty("amountOfLists" + Helper.returnFormatedDate(i));
            i--;
        }

        initTabsWayList();

        return v;
    }

    @Override
    public void onRefresh() {
        soapWayListIds = loadTodayList(getContext());
        todayWayListIds = parseSoapTodayListId(soapWayListIds);
        initTabsWayList();
        refreshLayout.setRefreshing(false);
    }


    @SuppressLint("NewApi")
    private void getGeneralMap() {
        getDataForMap();
        Intent intent = new Intent(this.getContext(), GeneralMap.class);
        intent.putExtra("id", todayWayListIds);
        startActivity(intent);
    }

    private SoapObject loadOrders(Context context, String id, String session) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
                    //загрузка путевого листа
                    DriverWayBill driverWayBill = new DriverWayBill(session, id);
                    driverWayBill.execute();
                    return driverWayBill.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getDataForMap() {
        for (String todayWayListId: todayWayListIds) {
            initData(getContext(), todayWayListId);
        }
    }

    @SuppressLint("NewApi")
    private void initData(Context context, String id) {
        SoapObject ordersSoap;
        JSONArray orderJson;
//        List<OrderMap> ordersMap = new ArrayList<>();
        ordersSoap = loadOrders(context, id, account.getSession());
        //endregion
        if (ordersSoap != null) {
            orderJson = soapToJSON(ordersSoap);
            SharedPreferencesStorage.addProperty("GeneralMap" + id, orderJson.toString());
        }
    }



    //загрузка путевых листов
    private static SoapObject loadTodayList(Context context) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
//                    TodayList todayList = new TodayList(account.getSession()); //для получения всех путивых листов
                    DriverTodayList todayList = new DriverTodayList(account.getId());
                    todayList.execute();
                    return todayList.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
        return null;
    }

    //превращаем SoapObject в массив
    private String[] parseSoapTodayListId(SoapObject todayWayLists) {
//        Log.d("FragmentContainer", soapWayListIds + "!!!!!!!!!!!!END IDS!!!!!!!!!!!");
        String[] wayListsId = new String[0];
        if (todayWayLists != null) {
            if (todayWayLists.getPropertyAsString("id").contains(","))
                wayListsId = todayWayLists.getPropertyAsString("id").replaceAll("\\s+", "").split(",");
            else {
                wayListsId = new String[1];
                wayListsId[0] = todayWayLists.getPropertyAsString("id").replaceAll("\\s+", "");
            }
            return wayListsId;
        }
        return wayListsId;
    }

    //путевые листы
    private void initTabsWayList() {
        if (todayWayListIds.length > 0) {
            setupContent(container, todayWayListIds);
            if (!SharedPreferencesStorage.checkProperty("amountOfLists" + Helper.returnFormatedDate(0)))
                SharedPreferencesStorage.addProperty("amountOfLists" + Helper.returnFormatedDate(0), String.valueOf(todayWayListIds.length));
            noWayBills.setVisibility(View.INVISIBLE);
        } else if (SharedPreferencesStorage.checkProperty("amountOfLists" + Helper.returnFormatedDate(0))) {
            int size = Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists" + Helper.returnFormatedDate(0)));
            todayWayListIds = new String[size];
            setupContent(container, todayWayListIds);
            noWayBills.setVisibility(View.INVISIBLE);
        } else
            noWayBills.setVisibility(View.VISIBLE);
    }

    //метод для установки содержимого вкладок
    private void setupContent(ViewPager2 content, String[] id) {
        ViewPagerAdapterTab adapter = new ViewPagerAdapterTab(this);
        if (id != null && id.length != 0) {
            Arrays.sort(id, String::compareTo);
        }
        adapter.initSharedPreferences(getContext());
        adapter.addArrays(id);


        assert id != null;
        for (String s : id) Log.d("FragmentContainer", "Way list " + s);

        content.setAdapter(adapter);

        new TabLayoutMediator(wayLists, content,
                (TabLayoutMediator.TabConfigurationStrategy) (tab, position) -> tab.setText("Путевой лист № " + (position + 1) )).attach();
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
//                Log.d("FragmentOrders", "ORDERS" + jsonArrayOrder.getString(f));
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



}
