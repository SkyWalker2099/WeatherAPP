package com.example.zzh.androidbestpractice.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Zzh on 2018/4/18.
 */

public class Now  {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{

        @SerializedName("txt")
        public String info;
        @SerializedName("code")
        public String code;

    }

}
