package com.steven.coolweather.util;

/**
 * Created by Steven on 2016/9/29.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}

