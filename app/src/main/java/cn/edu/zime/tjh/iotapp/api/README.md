# API 网络通信模块

本模块提供了应用程序与后端服务器进行网络通信的统一接口。

## 结构

API 模块包含三个主要类：

1. **ApiClient** - 提供底层HTTP请求功能
   - 处理GET/POST请求
   - 管理OkHttp客户端实例
   - 处理请求和响应的序列化/反序列化

2. **ApiCallback** - 回调接口
   - `onSuccess(JSONObject response)` - 请求成功时调用
   - `onFailure(String errorMsg)` - 请求失败时调用

3. **ApiService** - 封装所有API端点方法
   - 登录
   - 注册
   - 设备管理相关API
   - 其他业务功能API

## 使用方法

### 1. 在Activity中初始化ApiService

```java
private ApiService apiService;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.your_layout);
    
    // 初始化API服务
    apiService = ApiService.getInstance();
}
```

### 2. 调用API方法

```java
// 登录示例
apiService.login(username, password, new ApiCallback() {
    @Override
    public void onSuccess(JSONObject response) {
        // 处理成功响应
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");
            
            if (success) {
                // 登录成功处理
            } else {
                // 登录失败处理
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onFailure(String errorMsg) {
        // 处理请求失败
    }
});
```

### 3. 添加新的API方法

在 `ApiService` 类中添加新的API方法：

```java
/**
 * 新的API方法
 *
 * @param param1 参数1
 * @param param2 参数2
 * @param callback 回调接口
 */
public void newApiMethod(String param1, int param2, ApiCallback callback) {
    try {
        JSONObject requestBody = new JSONObject();
        requestBody.put("param1", param1);
        requestBody.put("param2", param2);
        
        apiClient.postJson("/endpoint/path", requestBody, callback);
    } catch (JSONException e) {
        callback.onFailure("创建请求数据失败: " + e.getMessage());
    }
}
```

## 注意事项

1. API调用应该在工作线程中执行，UI更新应该在主线程中执行（使用runOnUiThread）
2. 在需要处理JSONObject的地方添加try-catch以处理潜在的JSON解析错误
3. 对于大型应用，考虑使用更高级的网络库如Retrofit或Volley来替代OkHttp 