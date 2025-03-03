package cn.edu.zime.tjh.iotapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.edu.zime.tjh.iotapp.adapter.DetectionResultAdapter;
import cn.edu.zime.tjh.iotapp.api.ApiCallback;
import cn.edu.zime.tjh.iotapp.api.ApiService;
import cn.edu.zime.tjh.iotapp.model.DetectionResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;

public class YOLO extends AppCompatActivity {
    
    private static final String TAG = "YOLO";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    
    // 视图组件
    private ImageView ivDetectedImage;
    private RecyclerView rvDetectionResults;
    private TextView tvSummary;
    private Button btnTakePhoto;
    
    // 适配器
    private DetectionResultAdapter adapter;
    
    // 图片URI和检测结果
    private Uri photoUri;
    private List<DetectionResult> detectionResults = new ArrayList<>();
    private File photoFile;
    private String currentPhotoPath;
    
    // 进度对话框
    private ProgressDialog progressDialog;
    
    // 跟踪请求状态
    private boolean requestCompleted = false;
    private boolean requestCancelled = false;
    // 记录请求开始时间
    private long requestStartTime = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yolo);
        
        // 初始化视图
        initViews();
        
        // 处理Intent数据
        handleIntent();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保进度对话框被关闭，防止窗口泄漏
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        ivDetectedImage = findViewById(R.id.ivDetectedImage);
        rvDetectionResults = findViewById(R.id.rvDetectionResults);
        tvSummary = findViewById(R.id.tvSummary);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        
        // 设置RecyclerView
        rvDetectionResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetectionResultAdapter();
        rvDetectionResults.setAdapter(adapter);
        
        // 设置拍照按钮点击事件
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }
    
    /**
     * 处理Intent中的数据
     */
    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // 获取图片URI（如果是从其他界面传入的）
            if (intent.hasExtra("imageUri")) {
                String uriString = intent.getStringExtra("imageUri");
                if (uriString != null && !uriString.isEmpty()) {
                    photoUri = Uri.parse(uriString);
                    loadImage(photoUri);
                }
            }
        }
    }
    
    /**
     * 启动相机拍照
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // 确保有相机应用处理Intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 创建保存照片的File
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "创建图片文件失败", ex);
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
            }
            
            // 如果文件创建成功，继续拍照
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "cn.edu.zime.tjh.iotapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "未找到相机应用", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 创建保存图片的文件
     */
    private File createImageFile() throws IOException {
        // 创建图片文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* 前缀 */
                ".jpg",         /* 后缀 */
                storageDir      /* 目录 */
        );
        
        // 保存文件路径以供使用
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    /**
     * 处理相机返回的结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // 加载并显示图片
            loadImage(photoUri);
            
            // 上传图片到服务器进行检测
            uploadImageForDetection(photoFile);
        }
    }
    
    /**
     * 加载图片
     */
    private void loadImage(Uri uri) {
        try {
            // 从URI加载图片
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            ivDetectedImage.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "加载图片失败", e);
            Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 上传图片进行检测
     * @param imageFile 要检测的图片文件
     */
    private void uploadImageForDetection(File imageFile) {
        // 重置请求状态
        requestCompleted = false;
        requestCancelled = false;
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在进行物体检测，请稍候...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 记录请求开始时间
        requestStartTime = System.currentTimeMillis();
        Log.d(TAG, "开始检测请求时间: " + requestStartTime);

        // 添加超时处理
        final Handler timeoutHandler = new Handler(Looper.getMainLooper());
        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (!requestCompleted && !requestCancelled) {
                    Log.w(TAG, "检测请求执行了" + (System.currentTimeMillis() - requestStartTime) + "ms，超过了设定的超时时间");
                    
                    // 先标记请求被取消，防止回调再次处理
                    requestCancelled = true;
                    
                    // 安全地关闭对话框
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            try {
                                progressDialog.dismiss();
                                Toast.makeText(YOLO.this, "检测请求超时，请检查网络连接后重试", Toast.LENGTH_LONG).show();
                                Log.e(TAG, "检测请求超时");
                            } catch (Exception e) {
                                Log.e(TAG, "关闭进度对话框失败", e);
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "请求已完成或已取消，不需执行超时处理");
                }
            }
        };
        
        // 检查文件大小并压缩（如果需要）
        File fileToUpload = imageFile;
        long fileSize = imageFile.length();
        long maxFileSize = 2 * 1024 * 1024; // 2MB
        
        Log.d(TAG, "原始图片大小: " + (fileSize / 1024) + "KB");
        
        if (fileSize > maxFileSize) {
            try {
                // 显示压缩提示
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.setMessage("图片大小超过2MB，正在压缩...");
                    }
                });
                
                // 压缩图片并获取新的文件
                fileToUpload = compressImage(imageFile);
                Log.d(TAG, "压缩后图片大小: " + (fileToUpload.length() / 1024) + "KB");
            } catch (IOException e) {
                Log.e(TAG, "图片压缩失败", e);
                // 如果压缩失败，使用原始文件
                fileToUpload = imageFile;
            }
        }
        
        // 更新进度提示
        final File finalFileToUpload = fileToUpload;
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.setMessage("正在上传图片并进行检测，请稍候...");
            }
        });
        
        // 设置45秒超时 (增加超时时间以适应更大的图片或较慢的网络)
        timeoutHandler.postDelayed(timeoutRunnable, 45000);

        // 使用ApiService进行图像检测
        ApiService apiService = ApiService.getInstance();
        apiService.detectObjects(finalFileToUpload, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "接收到检测成功回调，耗时: " + (System.currentTimeMillis() - requestStartTime) + "ms");
                
                // 立即标记请求已完成
                requestCompleted = true;
                
                // 取消超时任务
                timeoutHandler.removeCallbacks(timeoutRunnable);
                
                Log.d(TAG, "检测成功: " + (response != null ? "响应不为空" : "响应为空!"));
                if (response != null) {
                    Log.d(TAG, "响应内容: " + response.toString());
                }
                
                // 安全地关闭对话框并处理结果
                runOnUiThread(() -> {
                    try {
                        // 只在未取消且对话框仍在显示时关闭
                        if (!requestCancelled && progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            
                            // 处理检测结果
                            if (response != null) {
                                processDetectionResult(response);
                            } else {
                                Toast.makeText(YOLO.this, "检测结果为空", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.w(TAG, "请求已被取消或进度对话框已关闭，不处理成功响应");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "处理检测成功结果出错", e);
                        Toast.makeText(YOLO.this, "处理检测结果时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "接收到检测失败回调，耗时: " + (System.currentTimeMillis() - requestStartTime) + "ms, 错误: " + errorMessage);
                
                // 立即标记请求已完成
                requestCompleted = true;
                
                // 取消超时任务
                timeoutHandler.removeCallbacks(timeoutRunnable);
                
                // 安全地关闭对话框并显示错误
                runOnUiThread(() -> {
                    try {
                        // 只在未取消且对话框仍在显示时关闭
                        if (!requestCancelled && progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            Toast.makeText(YOLO.this, "检测失败: " + errorMessage, Toast.LENGTH_LONG).show();
                        } else {
                            Log.w(TAG, "请求已被取消或进度对话框已关闭，不处理失败响应");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "处理检测失败结果出错", e);
                    }
                });
            }
        });
    }
    
    /**
     * 压缩图片到合适大小
     * @param imageFile 原始图片文件
     * @return 压缩后的图片文件
     */
    private File compressImage(File imageFile) throws IOException {
        // 创建压缩后的文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String compressedFileName = "COMPRESSED_" + timeStamp + ".jpg";
        File compressedFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), compressedFileName);
        
        // 读取原图
        Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        
        Log.d(TAG, "原始图片尺寸: " + originalWidth + "x" + originalHeight);
        
        // 计算缩放比例
        float scaleFactor = 1.0f;
        long maxFileSize = 2 * 1024 * 1024; // 2MB
        long fileSize = imageFile.length();
        
        if (fileSize > maxFileSize) {
            scaleFactor = (float) Math.sqrt((double) maxFileSize / fileSize * 0.8); // 确保在目标大小以下
        }
        
        // 缩放图片
        int targetWidth = (int) (originalWidth * scaleFactor);
        int targetHeight = (int) (originalHeight * scaleFactor);
        
        // 确保尺寸不小于最小值
        targetWidth = Math.max(targetWidth, 800);
        targetHeight = Math.max(targetHeight, 800);
        
        Log.d(TAG, "缩放后图片尺寸: " + targetWidth + "x" + targetHeight + ", 比例: " + scaleFactor);
        
        // 缩放图片
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);
        
        // 压缩质量
        int quality = 85; // 初始质量
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        
        // 如果压缩后仍然太大，继续降低质量
        while (outputStream.toByteArray().length > maxFileSize && quality > 30) {
            outputStream.reset();
            quality -= 10;
            Log.d(TAG, "调整压缩质量至: " + quality);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }
        
        // 写入文件
        FileOutputStream fos = new FileOutputStream(compressedFile);
        fos.write(outputStream.toByteArray());
        fos.flush();
        fos.close();
        
        // 回收位图资源
        if (scaledBitmap != originalBitmap) {
            scaledBitmap.recycle();
        }
        originalBitmap.recycle();
        
        return compressedFile;
    }
    
    /**
     * 处理检测结果
     */
    private void processDetectionResult(JSONObject response) {
        try {
            // 解析基本信息
            final int detectionId = response.getInt("detection_id");
            final int objectCount = response.getInt("object_count");
            final String resultImage = response.getString("result_image");
            
            // 解析检测数据
            JSONObject detectionData = response.getJSONObject("detection_data");
            JSONArray boxes = detectionData.getJSONArray("boxes");
            JSONArray confidences = detectionData.getJSONArray("confidences");
            JSONArray classIds = detectionData.getJSONArray("class_ids");
            JSONArray classNames = detectionData.getJSONArray("class_names");
            
            // 创建检测结果列表
            final List<DetectionResult> results = new ArrayList<>();
            
            // 创建类别计数映射
            Map<String, Integer> classCounts = new HashMap<>();
            
            // 处理每个检测结果
            for (int i = 0; i < classNames.length(); i++) {
                String className = classNames.getString(i);
                float confidence = (float) confidences.getDouble(i);
                
                // 统计每个类别的数量
                if (classCounts.containsKey(className)) {
                    classCounts.put(className, classCounts.get(className) + 1);
                } else {
                    classCounts.put(className, 1);
                }
            }
            
            // 将每个类别及其计数添加到结果列表
            for (Map.Entry<String, Integer> entry : classCounts.entrySet()) {
                String className = entry.getKey();
                int count = entry.getValue();
                
                // 找到该类别的最高置信度
                float maxConfidence = 0;
                for (int i = 0; i < classNames.length(); i++) {
                    if (className.equals(classNames.getString(i))) {
                        float confidence = (float) confidences.getDouble(i);
                        if (confidence > maxConfidence) {
                            maxConfidence = confidence;
                        }
                    }
                }
                
                // 添加到结果列表
                results.add(new DetectionResult(className, maxConfidence, count));
            }
            
            // 更新UI（必须在主线程中进行）
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 更新检测结果列表
                    detectionResults = results;
                    adapter.setResults(results);
                    
                    // 更新检测结果图片（如果有返回Base64图片数据）
                    if (resultImage != null && resultImage.startsWith("data:image")) {
                        try {
                            // 从Base64字符串加载图片
                            String base64Image = resultImage.split(",")[1];
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivDetectedImage.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            Log.e(TAG, "解析结果图片失败", e);
                        }
                    }
                    
                    // 更新摘要信息
                    String summary = String.format("检测ID: %d, 共检测到%d个物体，%d个类别",
                            detectionId, objectCount, results.size());
                    tvSummary.setText(summary);
                }
            });
            
        } catch (JSONException e) {
            Log.e(TAG, "解析检测结果失败", e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(YOLO.this, "解析检测结果失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * 显示检测结果
     */
    private void showDetectionResults() {
        if (detectionResults != null && !detectionResults.isEmpty()) {
            // 更新适配器
            adapter.setResults(detectionResults);
            
            // 更新摘要文本
            updateSummary();
        }
    }
    
    /**
     * 更新摘要信息
     */
    private void updateSummary() {
        int totalObjects = 0;
        for (DetectionResult result : detectionResults) {
            totalObjects += result.getCount();
        }
        
        String summary = String.format("总计: 检测到%d个物体，%d个类别", 
                totalObjects, detectionResults.size());
        tvSummary.setText(summary);
    }
}