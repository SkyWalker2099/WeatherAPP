package com.example.zzh.androidbestpractice.util;

import android.text.TextUtils;

import com.example.zzh.androidbestpractice.db.City;
import com.example.zzh.androidbestpractice.db.County;
import com.example.zzh.androidbestpractice.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Zzh on 2018/4/17.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static boolean handleProvinceResponse(String response){

        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvince = new JSONArray(response);
                for (int i =0; i<allProvince.length();i++){
                    JSONObject provinceObjet = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObjet.getString("name"));
                    province.setProvinceCode(provinceObjet.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();;
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities = new JSONArray(response);
                for(int i=0;i<allCities.length(); i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceid(provinceId);
                    city.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounty = new JSONArray(response);
                for(int i=0;i<allCounty.length(); i++){
                    JSONObject jsonObject = allCounty.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherid(jsonObject.getString("weather_id"));
                    county.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

}
