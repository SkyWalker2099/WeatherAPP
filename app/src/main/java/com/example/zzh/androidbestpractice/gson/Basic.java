package com.example.zzh.androidbestpractice.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Zzh on 2018/4/18.
 */

public class Basic  {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("lat")
    public double latitude;

    @SerializedName("lon")
    public double longitude;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;



    }

}
