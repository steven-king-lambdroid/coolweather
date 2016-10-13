package com.steven.coolweather.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.steven.coolweather.R;
import com.steven.coolweather.util.HttpCallbackListener;
import com.steven.coolweather.util.HttpUtil;
import com.steven.coolweather.util.Utility;

/**
 * Created by Steven on 2016/10/9.
 */
public class WeatherActivity extends Activity {
    private LinearLayout layoutWeatherInfo;
    private TextView textCityName;
    private TextView textPublish;
    private TextView textWeatherDesp;
    private TextView textTemp1;
    private TextView textTemp2;
    private TextView textCurrentDate;
    private static final String TAG = "Steven";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        layoutWeatherInfo = (LinearLayout)findViewById(R.id.weather_info_layout);
        textCityName = (TextView)findViewById(R.id.city_name_text);
        textPublish = (TextView)findViewById(R.id.publish_text);
        textWeatherDesp = (TextView)findViewById(R.id.weather_desp_text);
        textTemp1 = (TextView)findViewById(R.id.temp1_text);
        textTemp2 = (TextView)findViewById(R.id.temp2_text);
        textCurrentDate = (TextView)findViewById(R.id.current_date_text);

        //从ChooseAreaActivity中取出选中的countyCode
        String weatherCode = getIntent().getStringExtra("weather_code");
        //Log.d(TAG, "onCreate: weather_code = " + weatherCode);

        if(!TextUtils.isEmpty(weatherCode)){
            //有天气代码时就去查询天气
           // Log.d(TAG, "onCreate: **** 2");
            textPublish.setText("同步中...");
            layoutWeatherInfo.setVisibility(View.INVISIBLE);
            textCityName.setVisibility(View.INVISIBLE);
            queryWeatherInfo(weatherCode);       //根据countyCode查询对应的天气数据
        }
        else {
            //没有选中县级名字，直接显示本地天气
            showWeather();
        }
    }

    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        //Log.d(TAG, "queryWeatherInfo: address = " + address);
        queryFromServer(address, "weatherCode");

    }

    private void queryFromServer(final String address, final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                //Log.d(TAG, "****onFinish: response=" + response);
                if("weatherCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        //处理服务器返回的天气数据，保存在SharedPreference中
                        Utility.handleWeatherResponse(WeatherActivity.this, response);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather();
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textPublish.setText("同步失败");
                    }
                });
            }
        });
    }

    private void showWeather(){
        //Log.d(TAG, "showWeather: ****");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        textCityName.setText(sharedPreferences.getString("city_name", ""));
        textTemp1.setText(sharedPreferences.getString("temp1", ""));
        textTemp2.setText(sharedPreferences.getString("temp2", ""));
        textWeatherDesp.setText(sharedPreferences.getString("weather_desp", ""));
        textPublish.setText(sharedPreferences.getString("publish_time", ""));
        textCurrentDate.setText(sharedPreferences.getString("current_date", ""));
        layoutWeatherInfo.setVisibility(View.VISIBLE);
        textCityName.setVisibility(View.VISIBLE);
    }
}
