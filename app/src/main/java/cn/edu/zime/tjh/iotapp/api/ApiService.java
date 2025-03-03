package cn.edu.zime.tjh.iotapp.api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * API服务类，实现所有具体的后端API请求方法
 */
public class ApiService {
    private static final String TAG = "ApiService";
    
    // API端点常量
    private static final String ENDPOINT_LOGIN = "/auth/login";
    private static final String ENDPOINT_REGISTER = "/auth/register";
    private static final String ENDPOINT_RESET_PASSWORD = "/auth/reset-password";
    private static final String ENDPOINT_VERIFY_EMAIL = "/auth/verify-email";
    private static final String ENDPOINT_DETECT = "/detect";
    private static final String ENDPOINT_SENSOR_DATA = "/sensor/data";
    
    // 单例模式
    private static ApiService instance;
    private final ApiClient apiClient;
    
    /**
     * 私有构造函数
     */
    private ApiService() {
        apiClient = ApiClient.getInstance();
    }
    
    /**
     * 获取ApiService单例
     */
    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }
    
    /**
     * 测试API连接
     * 
     * @param callback 回调接口
     */
    public void testConnection(ApiCallback callback) {
        try {
            // 尝试请求服务器根路径
            apiClient.get("", callback);
        } catch (Exception e) {
            Log.e(TAG, "测试连接失败: " + e.getMessage());
            callback.onFailure("测试连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @param callback 回调接口
     */
    public void login(String username, String password, ApiCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);
            
            Log.d(TAG, "发送登录请求: " + ENDPOINT_LOGIN);
            Log.d(TAG, "请求体: " + requestBody.toString());
            
            apiClient.postJson(ENDPOINT_LOGIN, requestBody, callback);
        } catch (JSONException e) {
            Log.e(TAG, "创建登录请求失败: " + e.getMessage());
            callback.onFailure("创建请求数据失败: " + e.getMessage());
        }
    }
    /**
     * 获取注册用的邮箱验证码
     *
     * @param email    邮箱地址
     * @param callback 回调接口
     */
    public void getRegisterVerificationCode(String email, ApiCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("purpose", "register");

            Log.d(TAG, "发送获取注册验证码请求: " + ENDPOINT_VERIFY_EMAIL);
            Log.d(TAG, "请求体: " + requestBody.toString());

            apiClient.postJson(ENDPOINT_VERIFY_EMAIL, requestBody, callback);
        } catch (JSONException e) {
            Log.e(TAG, "创建获取注册验证码请求失败: " + e.getMessage());
            callback.onFailure("创建请求数据失败: " + e.getMessage());
        }
    }


    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @param email    邮箱地址
     * @param verificationCode 邮箱验证码
     * @param callback 回调接口
     */
    public void register(String username, String password, String email, String verificationCode, ApiCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);
            requestBody.put("email", email);
            requestBody.put("verification_code", verificationCode);

            Log.d(TAG, "发送注册请求: " + ENDPOINT_REGISTER);
            Log.d(TAG, "请求体: " + requestBody.toString());

            apiClient.postJson(ENDPOINT_REGISTER, requestBody, callback);
        } catch (JSONException e) {
            Log.e(TAG, "创建注册请求失败: " + e.getMessage());
            callback.onFailure("创建请求数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取邮箱验证码（用于找回密码 - 第一阶段）
     *
     * @param email    邮箱地址
     * @param callback 回调接口
     */
    public void getVerificationCode(String email, ApiCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            
            Log.d(TAG, "发送获取验证码请求: " + ENDPOINT_RESET_PASSWORD);
            Log.d(TAG, "请求体: " + requestBody.toString());
            
            apiClient.postJson(ENDPOINT_RESET_PASSWORD, requestBody, callback);
        } catch (JSONException e) {
            Log.e(TAG, "创建获取验证码请求失败: " + e.getMessage());
            callback.onFailure("创建请求数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置密码（第二阶段）
     *
     * @param email           邮箱地址
     * @param verificationCode 验证码
     * @param newPassword     新密码
     * @param callback        回调接口
     */
    public void resetPassword(String email, String verificationCode, String newPassword, ApiCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("verification_code", verificationCode);
            requestBody.put("new_password", newPassword);
            
            Log.d(TAG, "发送重置密码请求: " + ENDPOINT_RESET_PASSWORD);
            Log.d(TAG, "请求体: " + requestBody.toString());
            
            apiClient.postJson(ENDPOINT_RESET_PASSWORD, requestBody, callback);
        } catch (JSONException e) {
            Log.e(TAG, "创建重置密码请求失败: " + e.getMessage());
            callback.onFailure("创建请求数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用YOLO算法检测图像中的物体
     *
     * @param imageFile 包含要检测物体的图像文件
     * @param callback  回调接口
     */
    public void detectObjects(File imageFile, ApiCallback callback) {
        if (imageFile == null || !imageFile.exists()) {
            Log.e(TAG, "检测图像文件不存在或为空");
            callback.onFailure("图像文件不存在或无效");
            return;
        }

        Log.d(TAG, "准备上传图片进行检测，文件大小: " + (imageFile.length() / 1024) + "KB, 路径: " + imageFile.getAbsolutePath());
        
        try {
            // 验证文件可读
            if (!imageFile.canRead()) {
                Log.e(TAG, "图像文件不可读: " + imageFile.getAbsolutePath());
                callback.onFailure("图像文件不可读，请检查文件权限");
                return;
            }
            
            // 检查文件扩展名，确保是支持的图像格式
            String fileName = imageFile.getName().toLowerCase();
            if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
                  fileName.endsWith(".png") || fileName.endsWith(".bmp"))) {
                Log.w(TAG, "文件扩展名可能不是支持的图像格式: " + fileName);
            }
            
            // 根据文件扩展名确定正确的MediaType
            MediaType mediaType;
            if (fileName.endsWith(".png")) {
                mediaType = MediaType.parse("image/png");
            } else if (fileName.endsWith(".bmp")) {
                mediaType = MediaType.parse("image/bmp");
            } else {
                // 默认假设为JPEG
                mediaType = MediaType.parse("image/jpeg");
            }
            
            // 创建请求体
            RequestBody requestFile = RequestBody.create(mediaType, imageFile);
            Log.d(TAG, "已创建RequestBody，MediaType: " + mediaType);
            
            // 创建MultipartBody
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", imageFile.getName(), requestFile);
                    
            // 添加其他可能的参数
            builder.addFormDataPart("confidence", "0.4");  // 置信度阈值降低一点，增加检测灵敏度
            builder.addFormDataPart("format", "json");     // 返回格式
            
            // 构建完整的多部分请求体
            RequestBody requestBody = builder.build();
            Log.d(TAG, "已创建完整的MultipartBody请求体");
            
            // 发送请求
            Log.d(TAG, "正在发送图像检测请求到: " + ENDPOINT_DETECT);
            
            // 创建封装的回调来添加额外的日志
            ApiCallback wrappedCallback = new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d(TAG, "检测请求成功返回，包含数据: " + (response != null));
                    // 转发到原始回调
                    callback.onSuccess(response);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "检测请求失败: " + errorMessage);
                    // 转发到原始回调
                    callback.onFailure(errorMessage);
                }
            };
            
            // 使用ApiClient发送请求
            ApiClient.getInstance().postMultipart(ENDPOINT_DETECT, requestBody, wrappedCallback);
            
        } catch (Exception e) {
            Log.e(TAG, "构建检测请求失败", e);
            callback.onFailure("构建检测请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询检测历史详情
     *
     * @param detectionId 检测ID
     * @param callback    回调接口
     */
    public void getDetectionHistory(int detectionId, ApiCallback callback) {
        String endpoint = "/detect/history/" + detectionId;
        apiClient.get(endpoint, callback);
    }
    
    /**
     * 获取设备列表
     *
     * @param userId   用户ID
     * @param callback 回调接口
     */
    public void getDeviceList(String userId, ApiCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        
        apiClient.get("/device/list", params, callback);
    }
    
    /**
     * 添加设备
     *
     * @param deviceInfo 设备信息JSON对象
     * @param callback   回调接口
     */
    public void addDevice(JSONObject deviceInfo, ApiCallback callback) {
        apiClient.postJson("/device/add", deviceInfo, callback);
    }
    
    /**
     * 获取设备数据
     *
     * @param deviceId 设备ID
     * @param callback 回调接口
     */
    public void getDeviceData(String deviceId, ApiCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("deviceId", deviceId);
        
        apiClient.get("/device/data", params, callback);
    }

    /**
     * 提交传感器数据
     * @param data 包含传感器类型、数值和警报级别的JSON对象
     * @param callback 回调接口
     */
    public void submitSensorData(JSONObject data, ApiCallback callback) {
        try {
            Log.d(TAG, "正在提交传感器数据: " + data.toString());
            apiClient.postJson(ENDPOINT_SENSOR_DATA, data, callback);
        } catch (Exception e) {
            Log.e(TAG, "提交传感器数据时出错: " + e.getMessage());
            callback.onFailure("提交传感器数据时出错: " + e.getMessage());
        }
    }
} 