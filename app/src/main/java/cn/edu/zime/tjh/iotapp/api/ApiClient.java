package cn.edu.zime.tjh.iotapp.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * API客户端类，封装所有与后端的网络通信
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    
    // 服务器基础URL - 确保与后端服务器地址匹配
    private static final String BASE_URL = "http://shixun.tjh666.cn:6000/api";
    
    // 媒体类型常量
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    // OkHttpClient单例
    private static ApiClient instance;
    private final OkHttpClient client;
    private Context applicationContext;
    
    /**
     * 私有构造函数
     */
    private ApiClient() {
        // 配置OkHttpClient，设置连接、读和写超时
        client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // 增加到30秒
            .readTimeout(60, TimeUnit.SECONDS)     // 增加到60秒
            .writeTimeout(30, TimeUnit.SECONDS)    // 增加到30秒
            .build();
    }
    
    /**
     * 获取ApiClient单例
     */
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    /**
     * 设置应用程序上下文
     * 
     * @param context 应用程序上下文
     */
    public void setApplicationContext(Context context) {
        if (context != null) {
            this.applicationContext = context.getApplicationContext();
        }
    }
    
    /**
     * 检查网络连接状态
     *
     * @return 如果有网络连接返回true，否则返回false
     */
    public boolean isNetworkAvailable() {
        if (applicationContext == null) {
            Log.w(TAG, "应用程序上下文未设置，无法检查网络状态");
            return true; // 默认假设网络可用
        }
        
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        
        return false;
    }
    
    /**
     * 设置自定义OkHttpClient
     */
    public void setCustomClient(OkHttpClient customClient) {
        if (customClient != null) {
            this.client.connectionPool().evictAll(); // 清除旧连接
        }
    }
    
    /**
     * 构建完整的URL
     */
    private String buildUrl(String endpoint) {
        return BASE_URL + endpoint;
    }
    
    /**
     * 发送GET请求
     *
     * @param endpoint 请求端点
     * @param callback 回调接口
     */
    public void get(String endpoint, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "OkHttp IoTApp Client")
                .build();
        
        executeRequest(request, callback);
    }
    
    /**
     * 发送带查询参数的GET请求
     *
     * @param endpoint 请求端点
     * @param params   查询参数
     * @param callback 回调接口
     */
    public void get(String endpoint, Map<String, String> params, ApiCallback callback) {
        StringBuilder urlBuilder = new StringBuilder(buildUrl(endpoint));
        
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            boolean isFirst = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!isFirst) {
                    urlBuilder.append("&");
                }
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                isFirst = false;
            }
        }
        
        Request request = new Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "OkHttp IoTApp Client")
                .build();
        
        executeRequest(request, callback);
    }
    
    /**
     * 发送POST请求（JSON格式）
     *
     * @param endpoint 请求端点
     * @param jsonBody JSON请求体
     * @param callback 回调接口
     */
    public void postJson(String endpoint, JSONObject jsonBody, ApiCallback callback) {
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);
        
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "OkHttp IoTApp Client")
                .build();
        
        Log.d(TAG, "POST请求体: " + jsonBody.toString());
        Log.d(TAG, "完整URL: " + buildUrl(endpoint));
        executeRequest(request, callback);
    }
    
    /**
     * 发送POST请求（表单格式）
     *
     * @param endpoint 请求端点
     * @param params   表单参数
     * @param callback 回调接口
     */
    public void postForm(String endpoint, Map<String, String> params, ApiCallback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        
        RequestBody requestBody = formBuilder.build();
        
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "OkHttp IoTApp Client")
                .build();
        
        executeRequest(request, callback);
    }
    
    /**
     * 发送POST请求，带Multipart表单数据（支持文件上传）
     *
     * @param endpoint    API端点，相对于基础URL
     * @param requestBody Multipart请求体
     * @param callback    请求回调
     */
    public void postMultipart(String endpoint, RequestBody requestBody, ApiCallback callback) {
        // 构建完整URL
        String url = BASE_URL + endpoint;
        
        // 对于大文件上传，使用更长的超时时间
        OkHttpClient uploadClient = client.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)  // 增加写入超时到90秒
                .readTimeout(90, TimeUnit.SECONDS)   // 增加读取超时到90秒
                .build();
        
        // 构建Request对象
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "OkHttp Client")
                .post(requestBody)
                .build();
        
        Log.d(TAG, "POST Multipart请求 - URL: " + url);
        
        // 执行请求 - 使用专门的上传客户端
        executeMultipartRequest(uploadClient, request, callback);
    }
    
    /**
     * 执行Multipart HTTP请求（专用于文件上传）
     *
     * @param client    定制的OkHttpClient
     * @param request   请求对象
     * @param callback  回调接口
     */
    private void executeMultipartRequest(OkHttpClient client, Request request, final ApiCallback callback) {
        // 首先检查网络连接
        if (!isNetworkAvailable()) {
            Log.e(TAG, "无网络连接，无法发送请求");
            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onFailure("无网络连接，请检查您的网络设置后重试");
                });
            }
            return;
        }
        
        final long startTime = System.currentTimeMillis();
        Log.d(TAG, "开始发送Multipart请求到URL: " + request.url().toString() + " 时间: " + startTime);
        Log.d(TAG, "请求方法: " + request.method());
        Log.d(TAG, "请求头: " + request.headers().toString());
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final long endTime = System.currentTimeMillis();
                Log.e(TAG, "Multipart请求失败, 耗时: " + (endTime - startTime) + "ms, 原因: " + e.getMessage());
                Log.e(TAG, "异常详情: ", e);  // 添加异常堆栈跟踪
                
                if (callback != null) {
                    // 确保在主线程中执行回调
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onFailure(e.getMessage());
                    });
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final long endTime = System.currentTimeMillis();
                Log.d(TAG, "Multipart请求响应完成, 耗时: " + (endTime - startTime) + "ms, 状态码: " + response.code());
                
                String responseBody = "";
                try {
                    if (response.body() != null) {
                        responseBody = response.body().string();
                        Log.d(TAG, "成功读取响应体，长度: " + responseBody.length());
                    } else {
                        Log.w(TAG, "响应体为空");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "读取响应体失败: " + e.getMessage());
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            callback.onFailure("读取响应体失败: " + e.getMessage());
                        });
                    }
                    return;
                }
                
                Log.d(TAG, "响应状态码: " + response.code());
                Log.d(TAG, "响应头: " + response.headers().toString());
                
                final String finalResponseBody = responseBody;
                if (responseBody.length() > 1000) {
                    Log.d(TAG, "响应体(前1000字符): " + responseBody.substring(0, 1000) + "...");
                } else {
                    Log.d(TAG, "响应体: " + responseBody);
                }
                
                if (callback != null) {
                    // 确保在主线程中执行回调
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Log.d(TAG, "开始在主线程处理响应");
                        if (response.isSuccessful()) {
                            try {
                                if (finalResponseBody == null || finalResponseBody.isEmpty()) {
                                    Log.e(TAG, "响应体为空，无法解析JSON");
                                    callback.onFailure("服务器返回空响应");
                                    return;
                                }
                                
                                Log.d(TAG, "开始解析JSON响应");
                                JSONObject jsonResponse = new JSONObject(finalResponseBody);
                                Log.d(TAG, "JSON解析成功，准备执行成功回调");
                                callback.onSuccess(jsonResponse);
                                Log.d(TAG, "成功回调执行完成");
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON解析错误: " + e.getMessage());
                                Log.e(TAG, "原始响应体: " + finalResponseBody);
                                callback.onFailure("服务器返回数据格式错误: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "请求失败，状态码: " + response.code());
                            
                            // 尝试解析错误响应
                            String errorMessage = "请求失败，状态码: " + response.code();
                            if (!finalResponseBody.isEmpty()) {
                                try {
                                    JSONObject errorJson = new JSONObject(finalResponseBody);
                                    if (errorJson.has("message")) {
                                        errorMessage += " - " + errorJson.getString("message");
                                    } else if (errorJson.has("error")) {
                                        errorMessage += " - " + errorJson.getString("error");
                                    }
                                } catch (JSONException e) {
                                    // 如果响应不是JSON格式，使用原始响应
                                    if (finalResponseBody.length() < 100) {
                                        errorMessage += " - " + finalResponseBody;
                                    }
                                    Log.e(TAG, "错误响应解析失败: " + e.getMessage());
                                }
                            }
                            
                            callback.onFailure(errorMessage);
                        }
                    });
                }
            }
        });
    }
    
    /**
     * 执行HTTP请求
     *
     * @param request  请求对象
     * @param callback 回调接口
     */
    private void executeRequest(Request request, final ApiCallback callback) {
        // 首先检查网络连接
        if (!isNetworkAvailable()) {
            Log.e(TAG, "无网络连接，无法发送请求");
            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onFailure("无网络连接，请检查您的网络设置后重试");
                });
            }
            return;
        }
        
        final long startTime = System.currentTimeMillis();
        Log.d(TAG, "开始发送请求到URL: " + request.url().toString() + " 时间: " + startTime);
        Log.d(TAG, "请求方法: " + request.method());
        Log.d(TAG, "请求头: " + request.headers().toString());
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final long endTime = System.currentTimeMillis();
                Log.e(TAG, "请求失败, 耗时: " + (endTime - startTime) + "ms, 原因: " + e.getMessage());
                Log.e(TAG, "异常详情: ", e);  // 添加异常堆栈跟踪
                
                if (callback != null) {
                    // 确保在主线程中执行回调
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onFailure(e.getMessage());
                    });
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final long endTime = System.currentTimeMillis();
                Log.d(TAG, "请求响应完成, 耗时: " + (endTime - startTime) + "ms, 状态码: " + response.code());
                
                String responseBody = "";
                if (response.body() != null) {
                    responseBody = response.body().string();
                }
                
                Log.d(TAG, "响应状态码: " + response.code());
                Log.d(TAG, "响应头: " + response.headers().toString());
                
                final String finalResponseBody = responseBody;
                if (responseBody.length() > 1000) {
                    Log.d(TAG, "响应体(前1000字符): " + responseBody.substring(0, 1000) + "...");
                } else {
                    Log.d(TAG, "响应体: " + responseBody);
                }
                
                if (callback != null) {
                    // 确保在主线程中执行回调
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonResponse = new JSONObject(finalResponseBody);
                                Log.d(TAG, "准备执行成功回调");
                                callback.onSuccess(jsonResponse);
                                Log.d(TAG, "成功回调执行完成");
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON解析错误: " + e.getMessage());
                                Log.e(TAG, "原始响应体: " + finalResponseBody);
                                callback.onFailure("服务器返回数据格式错误: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "请求失败，状态码: " + response.code());
                            
                            // 尝试解析错误响应
                            String errorMessage = "请求失败，状态码: " + response.code();
                            if (!finalResponseBody.isEmpty()) {
                                try {
                                    JSONObject errorJson = new JSONObject(finalResponseBody);
                                    if (errorJson.has("message")) {
                                        errorMessage += " - " + errorJson.getString("message");
                                    } else if (errorJson.has("error")) {
                                        errorMessage += " - " + errorJson.getString("error");
                                    }
                                } catch (JSONException e) {
                                    // 如果响应不是JSON格式，使用原始响应
                                    if (finalResponseBody.length() < 100) {
                                        errorMessage += " - " + finalResponseBody;
                                    }
                                    Log.e(TAG, "错误响应解析失败: " + e.getMessage());
                                }
                            }
                            
                            callback.onFailure(errorMessage);
                        }
                    });
                }
            }
        });
    }
} 