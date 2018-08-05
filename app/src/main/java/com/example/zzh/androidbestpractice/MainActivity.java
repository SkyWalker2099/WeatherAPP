package com.example.zzh.androidbestpractice;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.zzh.androidbestpractice.db.City;
import com.example.zzh.androidbestpractice.db.County;
import com.example.zzh.androidbestpractice.db.Province;
import com.example.zzh.androidbestpractice.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog = null;

    android.app.Fragment fragment = (android.app.Fragment)getFragmentManager().findFragmentById(R.id.choose_area_fragment);

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SDKInitializer.initialize(getApplicationContext());

//        if(Connector.getDatabase()== null){
////            buildInformation();
//
//        }
//        Toast.makeText(getApplicationContext(), "加载地图22", Toast.LENGTH_SHORT).show();



        File file =  new File("/data/data/" + getApplicationContext().getPackageName() + "/databases");
        if(file != null && file.exists()&& file.isDirectory()){
            for(File item: file.listFiles()){
                item.delete();
            }
        }

        importdatabase();





//        buildInformation();

       // fragment.onResume();
//        Toast.makeText(getApplicationContext(), "加载地图33", Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getString("weatherids", null)!=null&& preferences.getString("weatherids",null) != ""){
            Log.d(TAG, "onCreate: "+ preferences.getString("weatherids",null));
            Intent intent = new Intent(this, TabActivity.class);
            startActivity(intent);
            finish();
        }


    }


    public void importdatabase(){


        String dirPath = "/data/data/com.example.zzh.androidbestpractice/databases";
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdir();
        }

        File file = new File(dir, "cool_weather.db");
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            InputStream is = this.getApplicationContext().getResources().openRawResource(R.raw.city_info);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            fos.write(buffer);
            is.close();
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }


    }



    public void buildInformation(){
         String provinceAddress = "http://guolin.tech/api/china";
//        showProgressDialog();


        HttpUtil.sendOkHttpRequest(provinceAddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                closeProgressDialog();
//                Toast.makeText(getApplicationContext(), "加载地图数据失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Toast.makeText(getApplicationContext(), "加载地图", Toast.LENGTH_SHORT).show();
                String responseText = response.body().string();
                if(!TextUtils.isEmpty(responseText)){
                    try{
                        JSONArray allProvince = new JSONArray(responseText);

                        for (int i = 0; i<allProvince.length(); i++){
                            JSONObject jsonObject = allProvince.getJSONObject(i);
                            Province province = new Province();

                            province.setProvinceName(jsonObject.getString("name"));

                            province.setProvinceCode(jsonObject.getInt("id"));
                            province.save();
//                            fragment.onResume();
                            buildCitiesInfos(province.getProvinceCode());

                            Thread.sleep(1000);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
//                    Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }


    public void buildCitiesInfos(final int provinceId){
         String address = "http://guolin.tech/api/china/"+provinceId;
//        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                closeProgressDialog();
//                Toast.makeText(getApplicationContext(), "加载地图数据失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Toast.makeText(getApplicationContext(), "加载地图", Toast.LENGTH_SHORT).show();
                String responseText = response.body().string();
                if(!TextUtils.isEmpty(responseText)){
                    try{
                        JSONArray allCities = new JSONArray(responseText);
                        for (int i =0; i<allCities.length(); i++){
                            City city = new City();
                            JSONObject jsonObject = allCities.getJSONObject(i);
                            city.setCityName(jsonObject.getString("name"));
                            city.setCityCode(jsonObject.getInt("id"));
                            city.setProvinceid(provinceId);
                            buildCountiesInfos(provinceId, city.getCityCode(), city);
                            Thread.sleep(100);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
//                    Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        closeProgressDialog();
    }

    public void buildCountiesInfos(int provincedId , final int cityId, final City city){
        String address = "http://guolin.tech/api/china/" + provincedId +"/"+cityId;
//        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                closeProgressDialog();
//                Toast.makeText(getApplicationContext(), "加载地图数据失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Toast.makeText(getApplicationContext(), "加载地图", Toast.LENGTH_SHORT).show();
                String responseText = response.body().string();
                if(!TextUtils.isEmpty(responseText)){
                    try{
                        JSONArray allCounties = new JSONArray(responseText);
                        for (int i =0; i<allCounties.length(); i++){

                            County county = new County();
                            JSONObject jsonObject = allCounties.getJSONObject(i);
                             county.setWeatherid(jsonObject.getString("weather_id"));
                            county.setCityId(cityId);
                            county.setCountyName(jsonObject.getString("name"));
                            county.save();
                            if(i==0){
                                city.setWeatherid(county.getWeatherid());
                                city.save();
                            }
                            Thread.sleep(10);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
//                    Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


//    private void showProgressDialog(){
//
//        if(progressDialog == null){
//            progressDialog = new ProgressDialog();
//            progressDialog.setMessage("正在加载");
//            progressDialog.setCanceledOnTouchOutside(false);
//        }
//        progressDialog.show();
//    }
//
//    private void closeProgressDialog(){
//
//        if(progressDialog!=null){
//            progressDialog.dismiss();
//        }
//
//    }


}
