package com.example.zzh.androidbestpractice;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zzh.androidbestpractice.gson.Weather;

import com.example.zzh.androidbestpractice.util.HttpUtil;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TabActivity extends AppCompatActivity {

    private String TAG = "TabActivity";

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private Button button;
    private Button area_button;
    private Button delete_area;
    private TextView city_name;
//    private TextView update_time;

    private ImageView bingPicImage;

    private List<String> tabIndicater;
    private List<android.support.v4.app.Fragment> tabFragments;
    private FragmentAdapter fragmentAdapter;

    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefreshLayout;

    private setting_center settingfragment;

    private boolean onResponse;

    private List<Weather> weathers = new ArrayList<>();
    private List<String> WeatherTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        button = (Button) findViewById(R.id.nav_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TabActivity.this, WeatherMap.class);
                startActivity(intent);
            }
        });

        area_button = (Button) findViewById(R.id.area_choose);
        area_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        delete_area = (Button)findViewById(R.id.area_delete);
        delete_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: tabcount"+tabLayout.getTabCount());
                int index = tabLayout.getSelectedTabPosition();
                int count = tabLayout.getTabCount();
                if(count!=1){
                    count-=1;
                    if(index< count){
                        viewPager.setCurrentItem(index+1, true);
                    }
                    else {
                        viewPager.setCurrentItem(index-1, true);
                    }
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TabActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                List<String> weatherids = Trans.str_to_list(preferences.getString("weatherids",null));
                weatherids.remove(index);
                editor.putString("weatherids", Trans.list_to_string(weatherids));
                editor.apply();

//                tabFragments.remove(index);
//                tabIndicater.remove(index);
//                tabFragments.remove(index);
//                tabLayout.removeTabAt(index);
                tabLayout.removeTabAt(index);
                fragmentAdapter.remove(index);
                fragmentAdapter.notifyDataSetChanged();

                Log.d(TAG, "onClick: tabcount"+tabLayout.getTabCount());
                Log.d(TAG, "onClick: index "+index);
                requestWeather();
                Log.d(TAG, "onClick: tabcount"+tabLayout.getTabCount());
                Log.d(TAG, "onClick: fragment"+tabFragments.size());
                Log.d(TAG, "onClick: tabindeciter"+tabIndicater.size());
                Log.d(TAG, "onClick: viewpager"+viewPager.getChildCount());

            }
        });


        city_name = (TextView) findViewById(R.id.title_city);
//        update_time = (TextView) findViewById(R.id.update_title_time);

        tabLayout = (TabLayout)findViewById(R.id.tab);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        tabFragments = new ArrayList<>();
        tabIndicater = new ArrayList<>();
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

//        initTitle(); //添加标题
//        initfragment(); //  添加fragment

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), tabFragments, tabIndicater );
        viewPager.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(fragmentAdapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Weather weather = weathers.get(tab.getPosition());
                city_name.setText(weather.basic.cityName);
//                update_time.setText(weather.basic.update.updateTime );
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

//        tabLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//            }
//        });




        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_swipe);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather();
            }


        });

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                drawerLayout.openDrawer(GravityCompat.START);
//            }
//        });

        try{
            String filePath = "/data/data/com.example.zzh.androidbestpractice/bg_pic.bmp";
            File file = new File(filePath);
            if(file.exists()){
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                Drawable drawable = new BitmapDrawable(bitmap);
                drawerLayout.setBackground(drawable);
             }
        }catch (Exception e){
            e.printStackTrace();
        }

        requestWeather();

    }

    @Override
    protected void onStart() {
        super.onStart();
        requestWeather();
    }

    private void initTitle(){
        tabIndicater = new ArrayList<>();
        tabIndicater.add("asdasd");
        tabIndicater.add("asdasda");
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addTab(tabLayout.newTab().setText(tabIndicater.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(tabIndicater.get(1)));
    }

    private void initfragment(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weather = sharedPreferences.getString("weather", null);
        tabFragments = new ArrayList<>();
        Log.d("", "initfragment: "+weather);
        tabFragments.add(WeatherFragment.newInstance(weather));
        tabFragments.add(WeatherFragment.newInstance(weather));
    }//

    public void requestWeather(){
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TabActivity.this);
        List<String> weatherids = Trans.str_to_list(preferences.getString("weatherids",null));
        final List<String> weathertexts = new ArrayList<>();
        for(String s : weatherids){
            Log.d("sssssssssssssssssss", "requestWeather: "+s);
        }
        if(weatherids.size()!=0 && weatherids != null){
            tabIndicater.clear();
            tabFragments.clear();
            tabLayout.removeAllTabs();
            weathers.clear();
            for(String weatherid: weatherids){
                String weathertext = preferences.getString(weatherid, null);
                if(weathertext != null && weathertext != ""){
//                    weathertexts.add(weathertext);
                }
                else {
                    String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherid + "&key=890df52e0dba4a40b096e44c148d640e";
                    Log.d("xxxxxxxxxxxxxxxxxxxxx", "requestWeather: "+weatherUrl);
                    HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                        
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            Toast.makeText(TabActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            onResponse = true;
                            String responseText = response.body().string();
                            Log.d(TAG, "onResponse: ~~~~~~~~~~~~~~~~~~~~~~");
                            Log.d("TabActivity", "onResponse: "+responseText);
                            Weather weather = HttpUtil.handleWeatherResponse(responseText);
                            if(weather!=null&&"ok".equals(weather.status)){
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(TabActivity.this).edit();
                                editor.putString(weather.basic.weatherId, responseText);
                                editor.apply();
                            }
                            onResponse = false;
                        }
                    });
                }
            }
        }else {
            Toast.makeText(this, "获取天气失败", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TabActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        for(String weatherid: weatherids){
            Log.d(TAG, "requestWeather: "+weatherid);
             String responsetext = null;
            while(responsetext == null)
                responsetext = preferences.getString(weatherid, null);
            weathertexts.add(responsetext);
            Log.d(TAG, "requestWeather: "+responsetext);
        }

        Log.d(TAG, "requestWeather: "+"--------------------");

        for(String weathertext: weathertexts){
            Log.d(TAG, "requestWeather: "+weathertext);
            weathers.add(HttpUtil.handleWeatherResponse(weathertext));
            tabIndicater.add("");
            tabLayout.addTab(tabLayout.newTab().setText("aaaaaaa"));
            tabFragments.add(WeatherFragment.newInstance(weathertext));
            fragmentAdapter.notifyDataSetChanged();
        }

        swipeRefreshLayout.setRefreshing(false);

//        Intent intent = new Intent(TabActivity.this, AutoUpdateService.class);
//        startService(intent);

    }


}
