package com.example.zzh.androidbestpractice;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.zzh.androidbestpractice.gson.Forecast;
import com.example.zzh.androidbestpractice.gson.Weather;
import com.example.zzh.androidbestpractice.util.HttpUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoView;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashTExt;

    private  TextView sportText;




    public WeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather, container ,false);





        weatherLayout = (ScrollView) view.findViewById(R.id.weather_layout);
//        titleCity = (TextView) view.findViewById(R.id.title_city);
//        titleUpdateTime = (TextView) view.findViewById(R.id.update_title_time);
        degreeText = (TextView) view.findViewById(R.id.dgree_text);
        weatherInfoView = (TextView) view.findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);
        aqiText = (TextView) view.findViewById(R.id.aqi_text);
        pm25Text = (TextView) view.findViewById(R.id.pm25_text);
        comfortText = (TextView) view.findViewById(R.id.comfort_text);
        carWashTExt = (TextView) view.findViewById(R.id.car_wash_text);
        sportText = (TextView) view.findViewById(R.id.sport_text);






        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        String weathertext = arguments.getString("weather_info");
        Log.d("WeatherFragment", "onActivityCreated: "+weathertext);
        Weather weather =  HttpUtil.handleWeatherResponse(weathertext);
        show(weather);

    }

    public static WeatherFragment newInstance(String weather){
        Bundle bundle = new Bundle();
        bundle.putString("weather_info", weather);
        WeatherFragment weatherFragment = new WeatherFragment();
        weatherFragment.setArguments(bundle);
        Log.d("WeatherFragment", "newInstance: "+ weather);
        return weatherFragment;
    }

    public void show(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime;
        String degree = weather.now.temperature+"摄氏度";
        String weatherInfo = weather.now.more.info;
        degreeText.setText(degree);
        weatherInfoView.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast: weather.forecastsList){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_item, forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }


        String comfort = "舒适度"+weather.suggestion.comfort.info;
        String carWash = "洗车指数"+ weather.suggestion.carWash.info;
        String sport = "运动指数"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashTExt.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }


}
