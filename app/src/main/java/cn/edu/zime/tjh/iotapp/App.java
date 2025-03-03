package cn.edu.zime.tjh.iotapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;

import cn.edu.zime.tjh.iotapp.api.ApiClient;

public class App  extends Application {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        Context mContext = this;
        
        // 初始化ApiClient
        try {
            ApiClient.getInstance().setApplicationContext(this);
            Log.d(TAG, "ApiClient初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "ApiClient初始化失败: " + e.getMessage(), e);
        }
        
        // 定位隐私政策同意
        AMapLocationClient.updatePrivacyShow(mContext,true,true);
        AMapLocationClient.updatePrivacyAgree(mContext,true);
        // 地图隐私政策同意
        MapsInitializer.updatePrivacyShow(mContext,true,true);
        MapsInitializer.updatePrivacyAgree(mContext,true);
        // 搜索隐私政策同意
        ServiceSettings.updatePrivacyShow(mContext,true,true);
        ServiceSettings.updatePrivacyAgree(mContext,true);
    }
}

