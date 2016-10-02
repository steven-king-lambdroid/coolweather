package com.steven.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.steven.coolweather.R;
import com.steven.coolweather.db.CoolWeatherDB;
import com.steven.coolweather.model.City;
import com.steven.coolweather.model.County;
import com.steven.coolweather.model.Province;
import com.steven.coolweather.util.HttpCallbackListener;
import com.steven.coolweather.util.HttpUtil;
import com.steven.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven on 2016/9/30.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ListView listView;
    private TextView textView;
    private CoolWeatherDB coolWeatherDB;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private int currentLevel;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private ProgressDialog progressDialog;

    private static final String TAG = "Steven";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView)findViewById(R.id.lv_content);
        textView = (TextView)findViewById(R.id.tv_title);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);       //ListView绑定adapter

        //获取CoolWeatherDB实例
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        if(coolWeatherDB == null){
            Log.d("Steven", "*** onCreate 1***");
        }

        //ListView Item监听器,加载市级和县级数据
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //点击ListView Item的响应操作
            }
        });
        //加载省级数据
        queryProvinces();

    }


    //1.1 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
    private void queryProvinces(){
        Log.d("Steven", "*** queryProvinces 1***");
        provinceList = coolWeatherDB.loadProvince();
        Log.d("Steven", "*** queryProvinces 2***");
        if(provinceList.size() > 0){
            dataList.clear();

            //从数据库中提取所有保存的省名，存放在dataList中,在ListView中显示
            for(Province province: provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged(); //?
            listView.setSelection(0);   //?
            textView.setText("中国");
            currentLevel = LEVEL_PROVINCE;

        } else {
            //数据库中没有数据时才去服务器上获取
            queryFromServer(null, "province");
        }
    }


    //1.2 根据传入的代号和类型从服务器上查询省市县数据。
    private void queryFromServer(final String code, final String type){
        String address;
        if(!TextUtils.isEmpty(code)){
            //请求市县级数据
            address = "http://www.weather.com.cn/data/city3jdata/provshi/" + code + ".xml";
        } else {
            //请求省级数据
            address = "http://www.weather.com.cn/data/city3jdata/china.html";
        }
        //自定义函数，开启ProgressDialog
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener()
        {
            //回调机制？？ response已经取得数据的字符串
            public void onFinish(String response){
                boolean result = false;
                //处理省级数据:保存到数据库中
                if("province".equals(type)){
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);

                    //处理市级数据:保存到数据库中
                } else if ("city".equals(type)){

                    //处理县级数据:保存到数据库中
                } else if ("county".equals(type)){

                }


                if(result){
                    //通过runOnUiThread()方法回到主线程处理逻辑:使用Handler或者AsyncTask同样可以实现
                    runOnUiThread(new Runnable(){
                        public void run(){
                            closeProgressDialog();
                            if("province".equals(type)){	//再次调用queryProvinces()函数;
                                queryProvinces();
                            }
                        }
                    });
                }
            }

            public void onError(Exception e){
                runOnUiThread(new Runnable(){
                    public void run(){
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }


    //显示进度对话框函数
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }



    //关闭对话框函数
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }


    //捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。
    public void onBackPressed(){
        if(currentLevel == LEVEL_COUNTY){
            //queryCities();
        } else if(currentLevel == LEVEL_CITY){
            //queryProvinces();
        } else{
            finish();	//当前是省级数据时直接退出
        }
    }












}
