package cn.edu.zime.tjh.iotapp.api;

import org.json.JSONObject;

/**
 * API回调接口，处理网络请求响应
 */
public interface ApiCallback {
    /**
     * 请求成功回调
     *
     * @param response JSON响应数据
     */
    void onSuccess(JSONObject response);

    /**
     * 请求失败回调
     *
     * @param errorMsg 错误信息
     */
    void onFailure(String errorMsg);
} 