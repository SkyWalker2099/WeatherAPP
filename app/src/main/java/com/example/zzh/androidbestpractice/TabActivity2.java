package com.example.zzh.androidbestpractice;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class TabActivity2 extends AppCompatActivity {

    private String TAG = "TabActivity";

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private Button button;
    private ImageView bingPicImage;

    private List<String> tabIndicater;
    private List<android.support.v4.app.Fragment> tabFragments;
    private FragmentAdapter fragmentAdapter;

    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab2);

        tabLayout = (TabLayout)findViewById(R.id.tab);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_swipe);

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        swipeRefreshLayout.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        swipeRefreshLayout.setEnabled(true);
                        break;
                }
                return false;

            }
        });

        tabFragments = new ArrayList<Fragment>();
        tabIndicater = new ArrayList<>();
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), tabFragments, tabIndicater );
        viewPager.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        List<String> weatherids = Trans.str_to_list(preferences.getString("weatherids",null));
         for (String weatherid: weatherids){
             String weather = preferences.getString(weatherid, null);
             tabIndicater.add("");
             tabLayout.addTab(tabLayout.newTab().setText("aaaaaaa"));
             tabFragments.add(WeatherFragment.newInstance(weather));
             fragmentAdapter.notifyDataSetChanged();
         }

    }



}
