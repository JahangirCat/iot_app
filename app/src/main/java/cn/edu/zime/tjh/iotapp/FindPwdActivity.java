package cn.edu.zime.tjh.iotapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.zime.tjh.iotapp.api.ApiCallback;
import cn.edu.zime.tjh.iotapp.api.ApiService;

public class FindPwdActivity extends AppCompatActivity {
    
    private static final String TAG = "FindPwdActivity";
    
    // 验证码倒计时时间（秒）
    private static final int COUNTDOWN_TIME = 60;
    
    // UI组件
    private TextInputLayout tilEmail;
    private TextInputLayout tilVerificationCode;
    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etVerificationCode;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnGetVerificationCode;
    private Button btnSubmit;
    private TextView tvBackToLogin;
    
    // 业务逻辑
    private ApiService apiService;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pwd);
        
        // 初始化API服务
        apiService = ApiService.getInstance();
        
        // 初始化UI组件
        initViews();
        
        // 设置事件监听器
        setupListeners();
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilVerificationCode = findViewById(R.id.tilVerificationCode);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        
        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        btnGetVerificationCode = findViewById(R.id.btnGetVerificationCode);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }
    
    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 获取验证码按钮点击事件
        btnGetVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForVerificationCode()) {
                    requestVerificationCode();
                }
            }
        });
        
        // 提交按钮点击事件
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForResetPassword()) {
                    resetPassword();
                }
            }
        });
        
        // 返回登录文本点击事件
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一页（登录页）
            }
        });
    }
    
    /**
     * 验证获取验证码所需的输入
     */
    private boolean validateForVerificationCode() {
        // 清除之前的错误提示
        tilEmail.setError(null);
        
        // 获取输入值
        String email = etEmail.getText().toString().trim();
        
        // 验证邮箱
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("请输入邮箱地址");
            return false;
        }
        
        // 验证邮箱格式
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("请输入有效的邮箱地址");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证重置密码所需的输入
     */
    private boolean validateForResetPassword() {
        // 首先验证获取验证码所需的输入
        if (!validateForVerificationCode()) {
            return false;
        }
        
        // 清除之前的错误提示
        tilVerificationCode.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
        
        // 获取输入值
        String verificationCode = etVerificationCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // 验证验证码
        if (TextUtils.isEmpty(verificationCode)) {
            tilVerificationCode.setError("请输入验证码");
            return false;
        }
        
        // 验证新密码
        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError("请输入新密码");
            return false;
        }
        
        // 验证新密码长度
        if (newPassword.length() < 6) {
            tilNewPassword.setError("密码长度至少为6位");
            return false;
        }
        
        // 验证确认密码
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("请确认新密码");
            return false;
        }
        
        // 验证两次密码输入是否一致
        if (!newPassword.equals(confirmPassword)) {
            tilConfirmPassword.setError("两次输入的密码不一致");
            return false;
        }
        
        return true;
    }
    
    /**
     * 请求获取验证码
     */
    private void requestVerificationCode() {
        // 获取输入值
        String email = etEmail.getText().toString().trim();
        
        // 显示进度提示
        Toast.makeText(this, "正在发送验证码...", Toast.LENGTH_SHORT).show();
        
        // 禁用获取验证码按钮并开始倒计时
        startCountdown();
        
        // 调用API发送获取验证码请求
        apiService.getVerificationCode(email, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "获取验证码成功: " + response.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 检查响应中是否包含错误信息
                            if (response.has("error")) {
                                String errorMsg = response.getString("error");
                                Toast.makeText(FindPwdActivity.this, "获取验证码失败: " + errorMsg, Toast.LENGTH_LONG).show();
                                // 取消倒计时
                                if (isTimerRunning) {
                                    countDownTimer.cancel();
                                    isTimerRunning = false;
                                    btnGetVerificationCode.setText("获取验证码");
                                    btnGetVerificationCode.setEnabled(true);
                                }
                            } else {
                                // 如果没有错误信息，则认为请求成功
                                String message = "验证码已发送";
                                if (response.has("message")) {
                                    message = response.getString("message");
                                }
                                Toast.makeText(FindPwdActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "解析获取验证码响应失败: " + e.getMessage());
                            Toast.makeText(FindPwdActivity.this, "验证码已发送，请查收邮件", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "获取验证码失败: " + errorMessage);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FindPwdActivity.this, "获取验证码失败: " + errorMessage, Toast.LENGTH_LONG).show();
                        // 如果获取失败，停止倒计时
                        if (isTimerRunning) {
                            countDownTimer.cancel();
                            isTimerRunning = false;
                            btnGetVerificationCode.setText("获取验证码");
                            btnGetVerificationCode.setEnabled(true);
                        }
                    }
                });
            }
        });
    }
    
    /**
     * 提交重置密码请求
     */
    private void resetPassword() {
        // 获取输入值
        String email = etEmail.getText().toString().trim();
        String verificationCode = etVerificationCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        
        // 显示进度提示
        Toast.makeText(this, "正在重置密码...", Toast.LENGTH_SHORT).show();
        
        // 禁用提交按钮
        btnSubmit.setEnabled(false);
        
        // 调用API发送重置密码请求
        apiService.resetPassword(email, verificationCode, newPassword, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "重置密码响应: " + response.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 检查响应中是否包含错误信息
                            if (response.has("error")) {
                                String errorMsg = response.getString("error");
                                Toast.makeText(FindPwdActivity.this, "密码重置失败: " + errorMsg, Toast.LENGTH_LONG).show();
                                // 重新启用提交按钮
                                btnSubmit.setEnabled(true);
                            } else {
                                // 如果响应中没有错误信息，则认为密码重置成功
                                String message = "密码重置成功";
                                if (response.has("message")) {
                                    message = response.getString("message");
                                }
                                Toast.makeText(FindPwdActivity.this, message + "，请使用新密码登录", Toast.LENGTH_LONG).show();
                                
                                // 密码重置成功，返回登录页
                                Intent intent = new Intent(FindPwdActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "解析重置密码响应失败: " + e.getMessage());
                            // 因为后端返回成功，我们假设密码已重置成功
                            Toast.makeText(FindPwdActivity.this, "密码已重置，请使用新密码登录", Toast.LENGTH_LONG).show();
                            
                            // 返回登录页
                            Intent intent = new Intent(FindPwdActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "重置密码失败: " + errorMessage);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FindPwdActivity.this, "重置密码失败: " + errorMessage, Toast.LENGTH_LONG).show();
                        // 重新启用提交按钮
                        btnSubmit.setEnabled(true);
                    }
                });
            }
        });
    }
    
    /**
     * 开始验证码倒计时
     */
    private void startCountdown() {
        // 禁用获取验证码按钮
        btnGetVerificationCode.setEnabled(false);
        
        // 创建并启动倒计时
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isTimerRunning = true;
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                btnGetVerificationCode.setText(secondsRemaining + "秒后重试");
            }
            
            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnGetVerificationCode.setEnabled(true);
                btnGetVerificationCode.setText("获取验证码");
            }
        }.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保释放倒计时资源
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}