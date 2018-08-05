package com.example.zzh.androidbestpractice;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class setting_center extends Fragment {

    private String TAG = "Setting_center";

    public boolean update_weather;

    public float time;

    private Button change_bg;

    private Switch update_sw;

    private EditText editText;

    private Button edit_button;

    private AutoUpdateService.MyBinder myBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (AutoUpdateService.MyBinder)service;
            Log.e(TAG, "onServiceConnected~~~~~~~~~~~~~~: connected");
            Toast.makeText(getActivity(),"connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected~~~~~~~~~~~~~~~~~: unconnected");
            Toast.makeText(getActivity(), "unconnected", Toast.LENGTH_SHORT).show();
        }
    };

    public setting_center() {
        // Required empty public constructor
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(getActivity(), "拒绝授权", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_center, container, false);

        change_bg = (Button) view.findViewById(R.id.background_button);
        update_sw = (Switch) view.findViewById(R.id.update_switch);
        editText = (EditText) view.findViewById(R.id.edit_text);
        edit_button = (Button) view.findViewById(R.id.edit_button);

//        Toast.makeText(getActivity(), "sssss", Toast.LENGTH_SHORT).show();
//        Intent bindIntent = new Intent(getActivity(), AutoUpdateService.class);
//        getActivity().getApplicationContext().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
//        Toast.makeText(getActivity(), "ddddd", Toast.LENGTH_SHORT).show();

        update_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TabActivity activity = (TabActivity) getActivity();
                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor edit = preference.edit();
                if(isChecked){
                    update_weather = true;
                    edit.putBoolean("update", update_weather);
                    edit.apply();
                    Intent intent = new Intent(activity, AutoUpdateService.class);
                    getActivity().getApplicationContext().startService(intent);
                    Intent bindIntent = new Intent(getActivity(), AutoUpdateService.class);
                    getActivity().getApplicationContext().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
                }else {
                    update_weather = false;
                    edit.putBoolean("update", update_weather);
                    edit.apply();
                    Intent intent = new Intent(activity, AutoUpdateService.class);
                    getActivity().getApplicationContext().unbindService(connection);
                    getActivity().getApplicationContext().stopService(intent);
                }
            }
        });



        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edittext = editText.getText().toString();
                if(edittext!= null && myBinder != null){
                    try{
                        float num = Float.parseFloat(edittext);
                        myBinder.changeupdate(num);
                    }catch (Exception e){
                        TabActivity activity = (TabActivity) getActivity();
                        Toast.makeText(activity, "设置出错", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        change_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });




        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        time = preference.getFloat("time", 0);
        update_weather = preference.getBoolean("update",false);


        update_sw.setChecked(update_weather);
        editText.setText(String.valueOf(time));

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TabActivity activity = (TabActivity) getActivity();

    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode){

             case 1:
                 if(data != null)
                 handleImageOnKitKat(data);

         }
    }


    private void handleImageOnKitKat(Intent data){

        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(getActivity(), uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID+ "="+id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri ,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
                imagePath = getImagePath(uri, null);
            }else if("file".equalsIgnoreCase(uri.getScheme())){
                imagePath =uri.getPath();
            }

            change_bg(imagePath);

        }

    private String getImagePath(Uri uri, String selection){

        String path = null;
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if(cursor!= null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void change_bg(String imagePath){

        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            String dirpath = "/data/data/com.example.zzh.androidbestpractice";
            File dir = new File(dirpath);
            if(!dir.exists()){
                dir.mkdir();
            }

            File file = new File(dir, "bg_pic.bmp");
            try{
                if(!file.exists()){
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100 ,byteArrayOutputStream);
                byte[] buffer = byteArrayOutputStream.toByteArray();
                fos.write(buffer);
                fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            Drawable drawable = new BitmapDrawable(bitmap);
            TabActivity activity = (TabActivity)getActivity();
            activity.drawerLayout.setBackground(drawable);
        }else {
            Toast.makeText(getActivity(), "更换背景失败", Toast.LENGTH_SHORT).show();
        }

    }



}
