package com.example.zzh.androidbestpractice;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomButtonsController;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.zzh.androidbestpractice.db.City;
import com.example.zzh.androidbestpractice.db.County;
import com.example.zzh.androidbestpractice.db.Province;
import com.example.zzh.androidbestpractice.gson.Weather;
import com.example.zzh.androidbestpractice.util.HttpUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherMap extends AppCompatActivity {

    private MapView mapView;

    private BaiduMap baiduMap;

    private  float currentZoom;

    private String province = "北京";

    private String city = "北京";

    private String TAG = "WeatherMap";

    private List<OverlayOptions> options;

    private GeoCoder mSearch;

    public LocationClient mLocationClient;

    private boolean ifFirstLocated = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        List<String> permissionList = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(WeatherMap.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(ContextCompat.checkSelfPermission(WeatherMap.this , Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if(ContextCompat.checkSelfPermission(WeatherMap.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!permissionList.isEmpty()){
            String[] permissions  = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherMap.this, permissions,1);
        }else {
            LocationClientOption option = new LocationClientOption();
            option.setScanSpan(5000);
            //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
            option.setIsNeedAddress(true);
            mLocationClient.setLocOption(option);
            mLocationClient.start();
        }



        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_weather_map);
        mapView = (MapView)findViewById(R.id.weather_map);

        baiduMap = mapView.getMap();

        mSearch = GeoCoder.newInstance();
//        baiduMap.showMapPoi(false);
//        showw();



        BaiduMap.OnMapStatusChangeListener listener = new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }
            // zoom>= 10 区级天气
            // >= 8 城市级天气
            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                showWeather(mapStatus);
            }
        }; //移动缩放地图监视器

        OnGetGeoCoderResultListener listener1 = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                province = reverseGeoCodeResult.getAddressDetail().province;
                province = province.substring(0, province.length()-1);
                city = reverseGeoCodeResult.getAddressDetail().city;
                city = city.substring(0 ,city.length()-1);
            }
        }; //地理编码

        baiduMap.setOnMapStatusChangeListener(listener); //设置监听器
        mSearch.setOnGetGeoCodeResultListener(listener1);

        showWeather(baiduMap.getMapStatus());

    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   if(bdLocation.getLocType()==BDLocation.TypeGpsLocation || bdLocation.getLocType()==BDLocation.TypeNetWorkLocation)
                       navigateTo(bdLocation);
               }
           });
        }

    }

    private void navigateTo(BDLocation location){
         if(ifFirstLocated){
             LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
             MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
             baiduMap.animateMapStatus(update);
             update = MapStatusUpdateFactory.zoomTo(12f);
             baiduMap.animateMapStatus(update);
             ifFirstLocated = false;
//             showWeather(baiduMap.getMapStatus());
             BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.location);
             OverlayOptions options = new MarkerOptions().position(ll).icon(bitmap);
             baiduMap.addOverlay(options);
             showWeather(baiduMap.getMapStatus());
         }
    }

    protected void showWeather(MapStatus mapStatus){
//        baiduMap.clear();
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(mapStatus.target));
        if (mapStatus.zoom>=8 && mapStatus.zoom <10){
            List<Province> provinces = DataSupport.where("provincename = ?",province ).find(Province.class);
            for (Province province: provinces){
                int provinceid = province.getId();
                List<City> cityList = DataSupport.where("provinceid = ?", Integer.toString(provinceid)).find(City.class);
                showCitiesWeather(cityList);
            }
        }
        if(mapStatus.zoom>=10){
            List<City> cities = DataSupport.where("cityname = ?",city ).find(City.class);
            for (City city: cities){
                int cityid = city.getId();
                List<County> countyList = DataSupport.where("cityid = ?", Integer.toString(cityid)).find(County.class);
                showCountiesWeather(countyList);
            }
        }
    }


    protected void showCitiesWeather(List<City> cities){
        for(City city: cities){
            String weatherurl = "http://guolin.tech/api/weather?cityid="+city.getWeatherid() + "&key=06b5bf5736c24261a0313b0f49bd4fab";
            HttpUtil.sendOkHttpRequest(weatherurl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Toast.makeText(WeatherMap.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final  String responseText = response.body().string();
                    final Weather weather = HttpUtil.handleWeatherResponse(responseText);
                    if(weather != null && "ok".equals(weather.status)){
                        LatLng point = new LatLng(weather.basic.latitude, weather.basic.longitude);
                        String num = "pic"+weather.now.more.code;
                        int resId = getResources().getIdentifier(num, "drawable", getPackageName());
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(resId);
                        OverlayOptions options = new MarkerOptions().position(point).icon(bitmap);
                        baiduMap.addOverlay(options);
                    }

                }
            });
        }

    }

    protected void showCountiesWeather(List<County> counties){

        for(County county: counties){
            String weatherurl = "http://guolin.tech/api/weather?cityid="+county.getWeatherid() + "&key=06b5bf5736c24261a0313b0f49bd4fab";
            HttpUtil.sendOkHttpRequest(weatherurl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Toast.makeText(WeatherMap.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final  String responseText = response.body().string();
                    final Weather weather = HttpUtil.handleWeatherResponse(responseText);
                    if(weather != null && "ok".equals(weather.status)){
                        LatLng point = new LatLng(weather.basic.latitude, weather.basic.longitude);
                        String num = "pic"+weather.now.more.code;
                        Log.d(TAG, "onResponse: "+num);
                        int resId = getResources().getIdentifier(num, "drawable", getPackageName());
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(resId);
                        OverlayOptions options = new MarkerOptions().position(point).icon(bitmap);
                        baiduMap.addOverlay(options);
                    }
                }
            });
        }


    }

    protected void showWeather(List<Weather> weathers){
        for(Weather weather: weathers){
            Log.d("WeatherMap", "showWeather: "+weather.basic.cityName+" "+weather.basic.weatherId);
            LatLng point = new LatLng(weather.basic.latitude, weather.basic.longitude);
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_back);
            OverlayOptions options = new MarkerOptions().position(point).icon(bitmap);
            baiduMap.addOverlay(options);
        }
    }   //未使用代码

    protected void showw(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getString("weather", null)!=null){
            String weatherString = preferences.getString("weather", null);
            Weather weather = HttpUtil.handleWeatherResponse(weatherString);
            show(weather);
        }
    }

    protected void show(Weather weather){
        LatLng point = new LatLng(weather.basic.latitude, weather.basic.longitude);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_back);
        OverlayOptions options = new MarkerOptions().position(point).icon(bitmap);
        baiduMap.addOverlay(options);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }
}
