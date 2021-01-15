package ru.iwater.yourwater.iwaterlogistic.ui.activities.map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.domain.Order;
import ru.iwater.yourwater.iwaterlogistic.domain.OrderMap;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class GeneralMap extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    JSONArray orderJson;
    List<Order> orderMaps;
    List<Order> activeOrderMaps;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_google_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        orderMaps = new ArrayList<>();

        Intent intent = getIntent();
        String[] todayWayListIds = intent.getStringArrayExtra("id");

        for (String todayWayListId: todayWayListIds) {
            if (SharedPreferencesStorage.checkProperty("GeneralMap" + todayWayListId)) {
                try {
                    orderJson = new JSONArray(SharedPreferencesStorage.getProperty("GeneralMap" + todayWayListId));
                    orderMaps.addAll(initOrders(orderJson));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        activeOrderMaps = parseOrder(orderMaps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map_view);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        int count = 0;


        for (int i = 0; i < activeOrderMaps.size(); i++) {
            Log.d("Map", "i = " + i);

            String time = activeOrderMaps.get(i).getTime();
            String period = activeOrderMaps.get(i).getPeriod();
            String[] coordinates = parseData(activeOrderMaps.get(i).getCoords());

            int num = count += 1;
            float latitude = 0.0F;
            float longitude = 0.0F;
            if (coordinates != null && !coordinates[0].equals("") && !coordinates[1].equals("")) {
                latitude = Float.parseFloat(coordinates[0]);
                longitude = Float.parseFloat(coordinates[1]);
                Log.d("Map", " coord2 = " + latitude + " " + longitude);
            } else return;
            LatLng point = new LatLng(latitude, longitude);

            if (period.contains("8:00-11:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(num, R.drawable.ic_icon_red))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else if (period.contains("9:00-13:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(num, R.drawable.ic_icon_yellow))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else if (period.contains("9:00-17:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).snippet(time).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(num, R.drawable.ic_icon_green))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else if (period.contains("13:00-17:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).snippet(time).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(num, R.drawable.ic_icon_violet))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else if (period.contains("18:00-20:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).snippet(time).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(num, R.drawable.ic_icon_blue))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(num, R.drawable.ic_icon_lightgray))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            }
        }
    }

    //метод для разбиения строчки в массив
    private String[] parseData(String data) {
        return data.split(",");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Order> parseOrder(List<Order> orders) {
        List<Order> activeOrder = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getStatus().equals("0")) {
                activeOrder.add(orders.get(i));
            }
        }
        return activeOrder;
    }

    //Кастомные иконки с номерами точек
    private Bitmap getCustomIcon(int count, int icon) {
        TextView text = new TextView(getApplicationContext());
        text.setText(String.valueOf(count));
        //центрирование номеров точек
        if (count > 10) {
            text.setPadding(47, 20, 0, 0);
            text.setTextSize(13);
        } else {
            text.setPadding(55, 18, 0, 0);
            text.setTextSize(14);
        }

        text.setGravity(Gravity.CENTER);

        IconGenerator generator = new IconGenerator(this);
        generator.setBackground(this.getResources().getDrawable(icon));
        generator.setContentView(text);
        return generator.makeIcon();
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
        sortOrder(orders);
        //удаление заказов
        ArrayList<Order> sortOrder = new ArrayList<>();
        for (int i = 0; i < orders.size() - 1; i++) {
            if(!orders.get(i).getId().equals(orders.get(i+1).getId())) {
                sortOrder.add(orders.get(i));
            }
        }
        sortOrder.add(orders.get(orders.size() - 1));
        return orders;
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


}
