package com.steven.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.steven.coolweather.db.CoolWeatherDB;
import com.steven.coolweather.model.City;
import com.steven.coolweather.model.County;
import com.steven.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Steven on 2016/9/29.
 */
public class Utility {
    //The synchronized keyword is all about different threads reading and writing to the same variables, objects and resources.
    //保证不同线程对该变量的操作是原子操作，A线程对它的改变，B线程实时可见


    /**
     * 解析从服务器返回的省级数据，并保存在数据库中
     *
     * @param db
     * @param response
     * @return
     */
    private static final String TAG = "Steven";
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB db, String response) {
        if (!TextUtils.isEmpty(response)) {
            response = response.replaceAll("\"", "").replaceAll("\\{", "").replaceAll("\\}", "");       //去除返回数据中的""和{}
            //Log.d("handleProvincesResponse", "*** response =  " + response + " ***");

            String[] allProvinces = response.split(",");   //String的split()方法，以,将字符串格式化为不同的字符串,得到“代号|城市”这样的字符串数组
            if (allProvinces != null && allProvinces.length > 0) {
                for (String string : allProvinces) {
                    String[] array = string.split(":");      //分别解析出省级代号和名字
                    Province province = new Province();
                    province.setProvinceCode(array[0]);         //设置省级代码
                    province.setProvinceName(array[1]);         //new added
                    db.saveProvince(province);                  //将解析出来的省级数据存放在数据库的Province表中
                }
                return true;
            }
        }
        return false;
    }


    /**
     * 解析从服务器返回的市级数据，并保存在数据库中
     *
     * @param db
     * @param response
     * @return
     */
    public synchronized static boolean handleCitiesResponse(CoolWeatherDB db, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            response = response.replaceAll("\"", "").replaceAll("\\{", "").replaceAll("\\}", "");       //去除返回数据中的""和{}
            //Log.d("handleCitiesResponse", "*** response =  " + response + " ***");
            String[] allCities = response.split(",");  //解析得到“代号|城市”这样的字符串数组
            if (allCities != null && allCities.length > 0) {
                for (String string : allCities) {
                    String[] array = string.split(":");  //分别解析市级代码和名字
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    db.saveCity(city);                  //将解析出来的县级数据存放在数据库的City表中
                }
                return true;
            }
        }


        return false;
    }

    /**
     * 解析从服务器返回的县级数据，并保存在数据库中
     *
     * @param db
     * @param response
     * @return
     */
    public synchronized static boolean handleCountiesResponse(CoolWeatherDB db, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            response = response.replaceAll("\"", "").replaceAll("\\{", "").replaceAll("\\}", "");       //去除返回数据中的""和{}
            String[] allCounties = response.split(",");    //解析得到“代号|城市”这样的字符串数组
            if (allCounties != null && allCounties.length > 0) {
                for (String string : allCounties) {
                    String[] array = string.split(":");  //分别解析县级代码和名字
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    db.saveCounty(county);       //将解析出来的县级数据存放在数据库的County表中
                }
                return true;
            }
        }

        return false;
    }


    /**
     * 解析从服务器返回的JSON数据，并将解析出的数据保存在本地
     *
     * @param context
     * @param response
     */
    public static void handleWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");

            //调用saveWeatherInfo函数将解析数据保存到SharedPreferences
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将解析数据保存到SharedPreferences
     *
     * @param context
     * @param cityName
     * @param weatherCode
     * @param temp1
     * @param temp2
     * @param weatherDesp
     * @param publishTime
     */
    public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        //editor.putString("current_date", simpleDateFormat.format(new Date()));
        Log.d(TAG, "saveWeatherInfo| city_name=  " + cityName);
        Log.d(TAG, "saveWeatherInfo| temp1=  " + temp1);
        Log.d(TAG, "saveWeatherInfo| temp2=  " + temp2);
        Log.d(TAG, "saveWeatherInfo| weather_desp=  " + weatherDesp);
        Log.d(TAG, "saveWeatherInfo| publish_time=  " + publishTime);
        //Log.d(TAG, "saveWeatherInfo| current_date=  " + simpleDateFormat.format(new Date()));
        editor.commit();
    }
}











