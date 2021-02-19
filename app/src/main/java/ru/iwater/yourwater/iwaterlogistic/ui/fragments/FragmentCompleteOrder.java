package ru.iwater.yourwater.iwaterlogistic.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.adapter.ListCompleteAdapter;
import ru.iwater.yourwater.iwaterlogistic.domain.Order;
import ru.iwater.yourwater.iwaterlogistic.remote.DriverWayBill;
import ru.iwater.yourwater.iwaterlogistic.utils.Check;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class FragmentCompleteOrder extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    RecyclerView rvCompleteOrder;
    TextView tvNoCompleteOrder;
    SwipeRefreshLayout refreshLayout;
    private ListCompleteAdapter listCompleteAdapter;
    private static ArrayList<Order> completeOrders;
    private String session;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferencesStorage.init(getContext());

        if (SharedPreferencesStorage.checkProperty("session")) {
            session = SharedPreferencesStorage.getProperty("session");
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complete_order, container, false);

        rvCompleteOrder = view.findViewById(R.id.list_complete_order);
        tvNoCompleteOrder = view.findViewById(R.id.no_complete_order);
        refreshLayout = view.findViewById(R.id.refresh_container_complete);
        refreshLayout.setOnRefreshListener(this);

        initDate();

        if (completeOrders.size() == 0) {
            rvCompleteOrder.setVisibility(View.GONE);
            tvNoCompleteOrder.setVisibility(View.VISIBLE);
        } else {
            rvCompleteOrder.setVisibility(View.VISIBLE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvCompleteOrder.setLayoutManager(linearLayoutManager);
            rvCompleteOrder.setAdapter(listCompleteAdapter);
            tvNoCompleteOrder.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onRefresh() {
        initDate();
        refreshLayout.setRefreshing(false);
    }

    private void initDate() {
        SoapObject ordersSoap = loadOrders(getContext());
        if (ordersSoap != null) {
            completeOrders = soapToArray(ordersSoap);
        } else completeOrders = new ArrayList<>();
        listCompleteAdapter = new ListCompleteAdapter(getContext(), completeOrders);
    }

    private SoapObject loadOrders(Context context) {
        if (Check.checkInternet(context)) {
            if (Check.checkServer(context)) {
                try {
                    //загрузка путевого листа
                    DriverWayBill driverWayBill = new DriverWayBill(session);
                    driverWayBill.execute();
                    return driverWayBill.get();
                } catch (Exception e) {
                    Log.e("iWater Logistic", "Получено исключение", e);
                }
            }
        }
        return null;
    }

    private ArrayList<Order> soapToArray(SoapObject ordersSoap) {
        ArrayList<Order> orders = new ArrayList<>();
        String idOrder, nameOrder, orderOrder, cashOrder, cash_bOrder, timeOrder, contactOrder, noticeOrder, dateOrder, periodOrder, addressOrder, coordsOrder, statusOrder;
        int id = 0, name = 0, order = 0, cash = 0, cash_b = 0, time = 0, contact = 0, notice = 0, date = 0, period = 0, address = 0, coords = 0, status = 0;
        for (int f = 0; f < ordersSoap.getPropertyCount() / 13; f++) {
            JSONObject jsonObject = new JSONObject();
            try {
                if (!ordersSoap.getPropertyAsString(id).equals("anyType{}"))
                    idOrder = ordersSoap.getPropertyAsString(id);
                else
                    idOrder = "";

                if (!ordersSoap.getPropertyAsString(name + 1).equals("anyType{}"))
                    nameOrder = ordersSoap.getPropertyAsString(name + 1);
                else
                    nameOrder = "";

                if (!ordersSoap.getPropertyAsString(order + 2).equals("anyType{}"))
                    orderOrder = ordersSoap.getPropertyAsString(order + 2);
                else
                    orderOrder = "";

                if (!ordersSoap.getPropertyAsString(cash + 3).equals("anyType{}"))
                    cashOrder = ordersSoap.getPropertyAsString(cash + 3);
                else
                    cashOrder = "0.00";

                if (!ordersSoap.getPropertyAsString(cash_b + 4).equals("anyType{}"))
                    cash_bOrder = ordersSoap.getPropertyAsString(cash_b + 4);
                else
                    cash_bOrder = "0.00";

                if (!ordersSoap.getPropertyAsString(time + 5).equals("anyType{}"))
                    timeOrder = ordersSoap.getPropertyAsString(time + 5);
                else
                    timeOrder = "";

                if (!ordersSoap.getPropertyAsString(contact + 6).equals("anyType{}"))
                    contactOrder = ordersSoap.getPropertyAsString(contact + 6);
                else
                    contactOrder = "";

                if (!ordersSoap.getPropertyAsString(notice + 7).equals("anyType{}"))
                    noticeOrder = ordersSoap.getPropertyAsString(notice + 7);
                else
                    noticeOrder = "";

                if (!ordersSoap.getPropertyAsString(date + 8).equals("anyType{}"))
                    dateOrder = ordersSoap.getPropertyAsString(date + 8).replaceAll("/+", "\\.");
                else
                    dateOrder = "";

                if (!ordersSoap.getPropertyAsString(period + 9).equals("anyType{}"))
                    periodOrder = ordersSoap.getPropertyAsString(period + 9);
                else
                    periodOrder = "";

                if (!ordersSoap.getPropertyAsString(address + 10).equals("anyType{}"))
                    addressOrder = ordersSoap.getPropertyAsString(address + 10);
                else
                    addressOrder = "";

                if (!ordersSoap.getPropertyAsString(coords + 11).equals("anyType{}"))
                    coordsOrder = ordersSoap.getPropertyAsString(coords + 11);
                else
                    coordsOrder = "";

                if (!ordersSoap.getPropertyAsString(status + 12).equals("anyType{}"))
                    statusOrder = ordersSoap.getPropertyAsString(status + 12);
                else
                    statusOrder = "";

                if (statusOrder.equals("1")) {
                    orders.add(new Order(idOrder, nameOrder, orderOrder, cashOrder, cash_bOrder, timeOrder, contactOrder, noticeOrder, dateOrder, periodOrder, addressOrder, statusOrder, coordsOrder));
                }

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

        return orders;

    }

    public static FragmentCompleteOrder newInstance() {
        return new FragmentCompleteOrder();
    }
}
