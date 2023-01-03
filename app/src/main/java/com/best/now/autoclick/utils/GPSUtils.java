package com.best.now.autoclick.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * author:zhoujingjin
 * date:2022/12/29
 */
public class GPSUtils {
    private static GPSUtils instance;
    private LocationManager locationManager;
    public static final int LOCATION_CODE = 1000;
    public static final int OPEN_GPS_CODE = 1001;

    public  String province = "";

    public static GPSUtils getInstance() {
        if (instance == null) {
            instance = new GPSUtils();
        }
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public String getProvince(Activity activity) {
        Log.i("GPS: ", "getProvince");
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);        // 默认Android GPS定位实例

        Location location = null;
        // 是否已经授权
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                }
            }

            location = bestLocation   ;        // 其他应用使用定位更新了定位信息 需要开启GPS
        }

        String p = "";
        if(location != null)
        {
            Log.i("GPS: ", "获取位置信息成功");
            Log.i("GPS: ","纬度：" + location.getLatitude());
            Log.i("GPS: ","经度：" + location.getLongitude());

            // 获取地址信息
            p = getAddress(location.getLatitude(),location.getLongitude(),activity);
            Log.i("GPS: ","location：" + p);
        }
        else
        {
            Log.i("GPS: ", "获取位置信息失败，请检查是够开启GPS,是否授权");
        }


        return p;
    }
    @TargetApi(Build.VERSION_CODES.M)
    public double[] getLocation(Activity activity) {
        Log.i("GPS: ", "getProvince");
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);        // 默认Android GPS定位实例
        double[] locations = new double[2];
        Location location = null;
        // 是否已经授权
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                }
            }

            location = bestLocation   ;
        }

        String p = "";
        if(location != null)
        {
            Log.i("GPS: ", "获取位置信息成功");
            Log.i("GPS: ","经度：" + location.getLatitude());
            Log.i("GPS: ","纬度：" + location.getLongitude());
            locations[0] = location.getLatitude();
            locations[1] = location.getLongitude();
        }
        else
        {
            Log.i("GPS: ", "获取位置信息失败，请检查是够开启GPS,是否授权");
        }
        return locations;
    }


    /*
     * 根据经度纬度 获取国家，省份
     * */
    public String getAddress(double latitude, double longitude, Activity activity) {
        String cityName = "";
        List<Address> addList = null;
        Geocoder ge = new Geocoder(activity);
        try {
            if (Geocoder.isPresent()){
                addList = ge.getFromLocation(latitude, longitude, 1);
            }else{
                return cityName;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addList != null && addList.size() > 0) {
            for (int i = 0; i < addList.size(); i++) {
                Address ad = addList.get(i);
                cityName += ad.getCountryName() + " " + ad.getLocality();
            }
        }
        return cityName;
    }
}