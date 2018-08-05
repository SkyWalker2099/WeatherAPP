package com.example.zzh.androidbestpractice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.example.zzh.androidbestpractice.gson.Weather;
import com.example.zzh.androidbestpractice.util.HttpUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
    
    private String TAG = "AUtoUpdateService";

    private float update_time = 1;



    private MyBinder myBinder = new MyBinder();

    class MyBinder extends Binder{

        public void changeupdate(float time){
            update_time = time;
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putFloat("time", time);
            editor.apply();
            Log.d(TAG, "changeupdate: "+"changeupdate"+this.toString());
        }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPIc();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anhour =  (int)(3600*8*1000*update_time);
        long triggerTime = SystemClock.elapsedRealtime() + anhour;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0 , intent1, 0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
        return super.onStartCommand(intent,flags,startId);
    }

    private void updateWeather(){
       SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        List<String> weatherids = Trans.str_to_list(preferences.getString("weatherids", null));

        if(weatherids.size()!=0 &&weatherids != null){
            for(String weatherid:  weatherids){
                String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherid + "&key=890df52e0dba4a40b096e44c148d640e";
//                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseText = response.body().string();
                        Weather weather = HttpUtil.handleWeatherResponse(responseText);
                        if(weather!=null&&"ok".equals(weather.status)){
                              SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                            editor.putString(weather.basic.weatherId, responseText);
                            editor.apply();

                        }
                    }
                });
            }
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String weatherString = preferences.getString("weather", null);
//        if(weatherString!= null){
//            Weather weather = HttpUtil.handleWeatherResponse(weatherString);
//            final String weatherId = weather.basic.weatherId;
//            String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId + "&key=06b5bf5736c24261a0313b0f49bd4fab";
//            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    String responseText = response.body().string();
//                    Weather weather = HttpUtil.handleWeatherResponse(responseText);
//                    if(weather!=null&&"ok".equals(weather.status)){
//                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
//                        editor.putString("weather", responseText);
//                        editor.apply();
//                    }
//                }
//            });
//        }

    }

    private void updateBingPIc(){

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });

    }

}
