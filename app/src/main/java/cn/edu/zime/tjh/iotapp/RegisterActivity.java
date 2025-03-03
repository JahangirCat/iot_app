package cn.edu.zime.tjh.iotapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.zime.tjh.iotapp.api.ApiCallback;
import cn.edu.zime.tjh.iotapp.api.ApiService;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText etUsername, etPassword, etConfirmPassword, etEmail, etVerificationCode;
    private Button btnRegister, btnGetVerificationCode;
    private Button btnTestConnection;
    private TextView tvMessage;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    private ApiService apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化API服务
        apiService = ApiService.getInstance();

        // 初始化视图
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        btnRegister = findViewById(R.id.btnRegister);
        btnGetVerificationCode = findViewById(R.id.btnGetVerificationCode);
        tvMessage = findViewById(R.id.tvMessage);
        
        // 创建测试连接按钮（如果布局中没有，可以动态添加）
//        addTestConnectionButton();

        // 设置获取验证码按钮点击事件
        btnGetVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "请先输入邮箱地址", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
                } else {
                    // 获取验证码
                    getVerificationCode(email);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();
                String email = etEmail.getText().toString();
                String verificationCode = etVerificationCode.getText().toString();

                // 表单验证
                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
                    email.isEmpty() || verificationCode.isEmpty()) {
                    tvMessage.setText("所有字段都必须填写");
                    tvMessage.setVisibility(View.VISIBLE);
                } else if (password.length() < 6) {
                    tvMessage.setText("密码至少需要6个字符");
                    tvMessage.setVisibility(View.VISIBLE);
                } else if (!password.equals(confirmPassword)) {
                    tvMessage.setText("两次输入的密码不一致");
                    tvMessage.setVisibility(View.VISIBLE);
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tvMessage.setText("请输入有效的邮箱地址");
                    tvMessage.setVisibility(View.VISIBLE);
                } else {
                    // 调用注册方法
                    register(username, password, email, verificationCode);
                }
            }
        });

        // 取消按钮
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一页
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消倒计时
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    /**
     * 获取邮箱验证码
     */
    private void getVerificationCode(String email) {
        // 禁用按钮，防止重复点击
        btnGetVerificationCode.setEnabled(false);
        
        // 显示加载消息
        tvMessage.setText("正在发送验证码...");
        tvMessage.setVisibility(View.VISIBLE);
        
        apiService.getRegisterVerificationCode(email, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "验证码发送成功: " + response.toString());
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String message = response.has("message") ? 
                                    response.getString("message") : "验证码已发送到您的邮箱";
                            
                            tvMessage.setText(message);
                            tvMessage.setVisibility(View.VISIBLE);
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            
                            // 开始倒计时
                            startCountDown();
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON解析错误: " + e.getMessage());
                            e.printStackTrace();
                            tvMessage.setText("服务器返回数据格式错误");
                            tvMessage.setVisibility(View.VISIBLE);
                            btnGetVerificationCode.setEnabled(true);
                        }
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "验证码发送失败: " + errorMsg);
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("验证码发送失败: " + errorMsg);
                        tvMessage.setVisibility(View.VISIBLE);
                        btnGetVerificationCode.setEnabled(true);
                    }
                });
            }
        });
    }

    /**
     * 开始倒计时
     */
    private void startCountDown() {
        if (isTimerRunning) {
            return;
        }
        
        isTimerRunning = true;
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnGetVerificationCode.setText(millisUntilFinished / 1000 + "秒");
            }

            @Override
            public void onFinish() {
                btnGetVerificationCode.setText("获取验证码");
                btnGetVerificationCode.setEnabled(true);
                isTimerRunning = false;
            }
        }.start();
    }

    private void testConnection() {
        tvMessage.setText("正在测试API连接...");
        tvMessage.setVisibility(View.VISIBLE);
        
        apiService.testConnection(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "API连接成功: " + response.toString());
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("API连接成功!");
                        tvMessage.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterActivity.this, "API连接正常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "API连接失败: " + errorMsg);
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("API连接失败: " + errorMsg);
                        tvMessage.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterActivity.this, "API连接失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void register(String username, String password, String email, String verificationCode) {
        Log.d(TAG, "调用注册API，用户名: " + username + ", 邮箱: " + email);
        tvMessage.setText("正在注册...");
        tvMessage.setVisibility(View.VISIBLE);
        
        apiService.register(username, password, email, verificationCode, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "注册成功: " + response.toString());
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 解析服务器返回的 JSON 数据
                            String message = response.has("message") ? 
                                    response.getString("message") : "注册成功";
                            
                            // 注册成功
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            finish(); // 关闭注册界面
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON解析错误: " + e.getMessage());
                            e.printStackTrace();
                            tvMessage.setText("服务器返回数据格式错误");
                            tvMessage.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "注册失败: " + errorMsg);
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 分析错误类型并提供更多诊断信息
                        if (errorMsg.contains("403")) {
                            tvMessage.setText("注册失败(403禁止访问): 可能是服务器配置限制或API路径错误。请检查网络日志。");
                        } else if (errorMsg.contains("404")) {
                            tvMessage.setText("注册失败(404未找到): API路径可能不正确。请检查端点路径。");
                        } else if (errorMsg.contains("Connection refused")) {
                            tvMessage.setText("注册失败: 无法连接到服务器。请确认服务器地址和端口正确，且服务器正在运行。");
                        } else {
                            tvMessage.setText("注册失败: " + errorMsg);
                        }
                        tvMessage.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}