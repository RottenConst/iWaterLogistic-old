package ru.iwater.yourwater.iwaterlogistic.ui.activities.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

import java.util.Arrays;

import ru.iwater.yourwater.iwaterlogistic.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    String[] coords;
    String[] times;
    String[] periods;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_google_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String coord = intent.getStringExtra("coordinates");
        String time = intent.getStringExtra("times");
        String period = intent.getStringExtra("period");

        assert coord != null;
        this.coords = parseData(coord);
        times = parseData(time);
        periods = parseData(period);

//        Log.d("Map", "names = " + Arrays.toString(times));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map_view);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private String[] parseData(String data) {
        return data.replace("[", "").replace("]", "").split(",");
    }

    @SuppressLint("NewApi")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        int count = 0;
//        Log.d("Map", "coord1 = " + coordinates[0] + " " +coordinates[1] + " count " + coordinates.length);
        for (int i  = 0; i < coords.length; i++) {
            Log.d("Map", "i = " + i);
            int i2 = i;
            String time = "";
            String period = "";
            if ( count < times.length) {
                time = times[count];
                period = periods[count];
                count +=1;
                Log.d("Map", "name " + count + " " + time + " " + period);
            }

            i2 += 1;
            Log.d("Map", "i2 = " + i2);
            float latitude = 0.0F;
            float longitude = 0.0F;
            if (i2 <= coords.length) {
                latitude = Float.parseFloat(coords[i]);
                longitude = Float.parseFloat(coords[i2]);
                Log.d("Map", " coord2 = " + latitude + " " + longitude);
            }
            LatLng point = new LatLng(latitude, longitude);

            if (period.contains("8:00-11:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(count, R.drawable.ic_icon_red))).title(String.valueOf(count)).snippet(time) ;
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else if (period.contains("9:00-13:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(count, R.drawable.ic_icon_yellow))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else if (period.contains("9:00-17:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).snippet(time).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(count, R.drawable.ic_icon_green))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else if (period.contains("13:00-17:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).snippet(time).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(count, R.drawable.ic_icon_violet))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else if (period.contains("18:00-20:59")) {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).snippet(time).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(count, R.drawable.ic_icon_blue))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            } else {
//                map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title(String.valueOf(count)).snippet(time));
                MarkerOptions mar = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(getCustomIcon(count, R.drawable.ic_icon_lightgray))).title(String.valueOf(count)).snippet(time);
                map.addMarker(mar);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0F));
            }
            i++;
        }
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
}
