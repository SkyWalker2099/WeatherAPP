package com.example.zzh.androidbestpractice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zzh on 2018/7/31.
 */

public class Trans {

    public  static List<String> str_to_list(String text){
        List<String> strings = new ArrayList<>();
        int a = 11;
        int k = text.length()/11;
        for(int i =0 ; i< k ;i++){
            String s = text.substring( i*a, i*a+ 11 );
            strings.add(s);
        }

        return strings;
    }

    public static String list_to_string(List<String> list){
        String s = "";
        for (String s1: list){
            s += s1;
        }
        return s;
    }

    public static Drawable str_to_draw(String str){

        if(str == null|| str.length() < 10)
            return null;

        byte[] img = Base64.decode(str.getBytes(), Base64.DEFAULT);
        Bitmap bitmap;
        if(img != null){
            bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
            Drawable drawable = new BitmapDrawable(bitmap);
            return drawable;
        }
        return null;
    }


    public static String draw_to_str(Drawable drawable){
        if(drawable!= null){
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE? Bitmap.Config.ARGB_8888: Bitmap.Config.ARGB_8888 );
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            int size = bitmap.getWidth()*bitmap.getHeight()*4;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(size);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 ,byteArrayOutputStream);
            byte[] imgdata = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(imgdata, Base64.DEFAULT);
        }

        return "";
    }

    public static boolean isNetworkConnected(Context context){
        if(context!= null){
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null){
                return true;
            }

        }
        return false;
    }


}
