package ru.iwater.yourwater.iwaterlogistic.adapter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentOrders;
import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class ViewPagerAdapterTab extends FragmentStateAdapter {

    private static final String TAG = "ViewAdapterTag";

    String[] idWayList;

    public ViewPagerAdapterTab(@NonNull Fragment fragment) {
        super(fragment);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle data = new Bundle();
        data.putInt("position",position);
        if(idWayList != null) {
            data.putString("waybill_id", idWayList[position]);
            SharedPreferencesStorage.addProperty("idwaylist" + position, idWayList[position]);
        }
        FragmentOrders fragment = FragmentOrders.newInstance(idWayList[position], position);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public int getItemCount() {
        if(idWayList != null)
            return idWayList.length;
        else if(SharedPreferencesStorage.checkProperty("amountOfLists"+Helper.returnFormatedDate(0))){
            return Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists"+Helper.returnFormatedDate(0)));
        }
        return 0;
    }

    public void addArrays(String[] idWayList) {
        this.idWayList = idWayList;
        if(SharedPreferencesStorage.checkProperty("amountOfLists"+ Helper.returnFormatedDate(0))) {
            int k = Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists" + Helper.returnFormatedDate(0)));
        }
    }

    public void initSharedPreferences(Context context){
        SharedPreferencesStorage.init(context);
    }
}
