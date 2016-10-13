package com.steven.coolweather.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Steven on 2016/9/29.
 */
public class HttpUtil {
    private static final String TAG = "Steven";
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
       // Log.d(TAG, "**** sendHttpRequest 1: listener = " + listener);
        //开启子线程处理连接服务器的耗时任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                //设置Http访问参数，连接服务器
                try {
                    URL url = new URL(address);
                    Log.d("TAG", "run 1 : address = " + address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    //此处不能用StringBuilder，否则部分天气数据解析不出报EOFException
                    StringBuffer response = new StringBuffer();

                    String line;
                    //连接服务器，读取全部数据,以字符串的形式存放在response.

                    Log.d(TAG, "******* reader = " + reader);
                    while ((line = reader.readLine()) != null) {
                        //line = reader.readLine();
                        Log.d(TAG, "line = " + line);
                        response.append(line);
                    }
                    Log.d(TAG, "-----------");
                    //Log.d(TAG, "*******run: listener="+listener);
                    if (listener != null) {        //正常响应的数据回调onFinish()方法,来进行数据处理逻辑
                        listener.onFinish(response.toString());
                    }

                } catch (Exception e) {          //异常响应，回调onError()方法
                    if (listener != null) {
                        listener.onError(e);
                    }


                } finally {
                    //关闭连接
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
