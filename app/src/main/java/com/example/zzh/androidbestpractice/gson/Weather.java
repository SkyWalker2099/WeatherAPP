package com.example.zzh.androidbestpractice.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Zzh on 2018/4/18.
 */

public class Weather {

    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastsList;

}
