package ru.iwater.yourwater.iwaterlogistic.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.iwater.yourwater.iwaterlogistic.utils.Helper;
import ru.iwater.yourwater.iwaterlogistic.utils.SharedPreferencesStorage;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private String[] Id;
    private ArrayList<String> Name;
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle data = new Bundle();
        data.putInt("position",position);
        if(Id != null)
            data.putString("waybill_id",Id[position]);
        fragments.get(position).setArguments(data);
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        if(Id != null)
            return Id.length;
        else if(SharedPreferencesStorage.checkProperty("amountOfLists"+Helper.returnFormatedDate(0))){
            return Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists"+Helper.returnFormatedDate(0)));
        }
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(Name != null && Name.size()>0)
            return Name.get(position);
        return null;
    }

    public void addArrays(String[] id) {
        this.Id = id;
        Name = new ArrayList<String>();
        if(Id != null)
            for(int i=0; i<Id.length; i++)
                Name.add("Путевой лист №"+String.valueOf(i+1));
        else if(SharedPreferencesStorage.checkProperty("amountOfLists"+Helper.returnFormatedDate(0))){
            int k = Integer.parseInt(SharedPreferencesStorage.getProperty("amountOfLists"+Helper.returnFormatedDate(0)));
            for(int i=0; i<k; i++)
                Name.add("Путевой лист №"+String.valueOf(i+1));
        }
    }

    public void initSharedPreferences(Context context){
        SharedPreferencesStorage.init(context);
    }

    public void addFragments(Fragment fragment){
        fragments.add(fragment);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}




