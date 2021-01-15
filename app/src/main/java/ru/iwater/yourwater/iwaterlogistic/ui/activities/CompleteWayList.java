package ru.iwater.yourwater.iwaterlogistic.ui.activities;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import ru.iwater.yourwater.iwaterlogistic.ui.fragments.FragmentCompleteWayList;
import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.adapter.ViewPagerAdapter;

public class CompleteWayList extends AppCompatActivity {

    //region ПЕРЕМЕННЫЕ
    private String[] id;//дата путевого листа
    private String formatedDate = "";//форматированная дата путевого листа
    private TabLayout wayLists;//вкладки
    private ViewPager content;//контейнер для вкладок
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_way_list);
        Log.d("CompleteWay", "OnCreate");
        Intent intent = getIntent();

        if(intent.getStringExtra("id")!=null) {
            if (intent.getStringExtra("id").contains(","))
                id = intent.getStringExtra("id").split(",");
            else {
                id = new String[1];
                id[0] = intent.getStringExtra("id");
            }
        }
        formatedDate = intent.getStringExtra("formated_date");

        //region кнопка возврата на предыдущий activity
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Путевой лист " + formatedDate);
        }
        //endregion

        wayLists = (TabLayout) findViewById(R.id.tabLayout1);
        content = (ViewPager) findViewById(R.id.viewPager1);

        wayLists.setupWithViewPager(content);

        setupContent(content,id);

    }

    //метод для установки содержимого вкладок
    private void setupContent(ViewPager content, String[] id) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.initSharedPreferences(getApplicationContext());
        adapter.addArrays(id);
        for(int i=0; i<id.length; i++)
            adapter.addFragments(new FragmentCompleteWayList());
        content.setAdapter(adapter);
    }

    //нажатие по кнопке перехода назад и выхода
    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
