package com.example.zzh.androidbestpractice.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Zzh on 2018/4/17.
 */

public class County extends DataSupport {

    private int id;
    private String countyName;
    private String weatherid;

    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherid() {
        return weatherid;
    }

    public void setWeatherid(String weatherid) {
        this.weatherid = weatherid;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
