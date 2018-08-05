package com.example.zzh.androidbestpractice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zzh.androidbestpractice.db.City;
import com.example.zzh.androidbestpractice.db.County;
import com.example.zzh.androidbestpractice.db.Province;
import com.example.zzh.androidbestpractice.gson.Weather;
import com.example.zzh.androidbestpractice.util.HttpUtil;

import org.litepal.crud.DataSupport;
import org.litepal.util.DBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * Created by Zzh on 2018/4/18.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVIENCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog = null;
    private TextView titleView ;
    private Button backButton;



    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datalist = new ArrayList<>();

    //省列表
    private List<Province>provinceList;
    //城市列表
    private List<City>cityList;
    //区列表
    private List<County>countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //选中的县区
    private County selectedCounty;

    //当前选中的级别
    public int currentLevel;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.choose_area, container, false);
        titleView = (TextView)view.findViewById(R.id.title_text);
        backButton= (Button)view.findViewById(R.id.back_button);

        listView = (ListView)view.findViewById(R.id.list_view);


        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, datalist);
        listView.setAdapter(adapter);
        return view;

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        queryProvinces();
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVIENCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    if(Trans.isNetworkConnected(getActivity())!= false){
                        String weatherId = countyList.get(position).getWeatherid();
                        if(getActivity() instanceof MainActivity){
                            Intent intent = new Intent(getActivity(), TabActivity.class);
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("weatherids", weatherId);
                            editor.apply();
                            startActivity(intent);
                        }else if(getActivity() instanceof WeatherActivity){  //instanceof 用于判断是否是某个类的实例
                            WeatherActivity activity = (WeatherActivity)getActivity();
                            activity.drawerLayout.closeDrawers();
                            activity.swipeRefreshLayout.setRefreshing(true);
                            activity.requestWeather(weatherId);
                        }else if(getActivity() instanceof TabActivity) {
                            TabActivity activity = (TabActivity)getActivity();
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            List<String> weatherids = Trans.str_to_list(preferences.getString("weatherids", null));
                            boolean a = false;
                            for(String weatherid: weatherids){
                                if (weatherid.equals(weatherId))
                                    a = true;
                            }
                            if(!a){
                                weatherids.add(weatherId);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("weatherids", Trans.list_to_string(weatherids));
                                editor.apply();
                            }else {
                                Toast.makeText(getContext(), "该城市已有", Toast.LENGTH_SHORT).show();
                            }
//
                            activity.drawerLayout.closeDrawers();
                            activity.swipeRefreshLayout.setRefreshing(true);
                            activity.requestWeather();
                        }
                    }else {
                        Toast.makeText(getActivity(),"网络出错", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });




        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();

    }

    public void queryProvinces(){
        titleView.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
//        if(provinceList.size()>0){
            datalist.clear();
            for(Province province: provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVIENCE;
//        }else {
//            String address = "http://guolin.tech/api/china";
//            queryFromServer(address, "province");
//        }
    }


    private void queryCities(){
        titleView.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
//        if(cityList.size()>0){
            datalist.clear();
            for(City city: cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
//        }else {
//            int provinceCode = selectedProvince.getProvinceCode();
//            String address = "http://guolin.tech/api/china/"+provinceCode;
//            queryFromServer(address, "city");
//        }
    }

    private void queryCounties(){

        titleView.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
//        if(countyList.size()>0){
            datalist.clear();
            for(County county : countyList){
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
//        }
//        else {
//            int provinceCode = selectedProvince.getProvinceCode();
//            int cityCode = selectedCity.getCityCode();
//            String address = "http://guolin.tech/api/china/" + provinceCode +"/"+cityCode;
//            queryFromServer(address, "county");
//        }
    }

    private void queryFromServer(String address, final String type){

        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = HttpUtil.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = HttpUtil.handleCityResponse(responseText, selectedProvince.getId());
                }else if("county".equals(type)){
                    result = HttpUtil.handleCountyResponse(responseText, selectedCity.getId());
                }

                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }
                            else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });

    }

    private void showProgressDialog(){

        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
       // progressDialog.show();
    }

    private void closeProgressDialog(){

        if(progressDialog!=null){
            progressDialog.dismiss();
        }

    }




}






















