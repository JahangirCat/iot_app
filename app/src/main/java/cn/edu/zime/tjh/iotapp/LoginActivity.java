package cn.edu.zime.tjh.iotapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnFindPwd;
    private TextView tvMessage;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化API服务
        apiService = ApiService.getInstance();

        // 初始化视图
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnFindPwd = findViewById(R.id.btnFindPwd);
        tvMessage = findViewById(R.id.tvMessage);


        //注册点击
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到注册界面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        
        // 找回密码按钮点击事件
        btnFindPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到找回密码界面
                Intent intent = new Intent(LoginActivity.this, FindPwdActivity.class);
                startActivity(intent);
            }
        });

        // 登录按钮点击事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    tvMessage.setText("用户名和密码不能为空");
                    tvMessage.setVisibility(View.VISIBLE);
                } else {
                    // 发起登录请求
                    Log.d(TAG, "开始登录用户: " + username);
                    login(username, password);
                }
            }
        });
    }

    private void login(String username, String password) {
        Log.d(TAG, "调用登录API，用户名: " + username);
        
        apiService.login(username, password, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "登录成功: " + response.toString());
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 解析服务器返回的 JSON 数据
                            String message = "登录成功";
                            String accessToken = "";
                            String role = "user";
                            
                            if (response.has("message")) {
                                message = response.getString("message");
                            }
                            
                            // 获取access_token和role
                            if (response.has("data")) {
                                JSONObject data = response.getJSONObject("data");
                                if (data.has("access_token")) {
                                    accessToken = data.getString("access_token");
                                }
                                if (data.has("role")) {
                                    role = data.getString("role");
                                }
                            }
                            
                            // 保存用户信息到SharedPreferences
                            saveUserInfo(username, accessToken, role);
                            
                            // 提示登录成功
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            
                            // 登录成功，跳转到主页
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
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
                Log.e(TAG, "登录失败: " + errorMsg);
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("登录失败: " + errorMsg);
                        tvMessage.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
    
    /**
     * 保存用户信息到SharedPreferences
     */
    private void saveUserInfo(String username, String accessToken, String role) {
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        // 保存用户信息
        editor.putString("username", username);
        editor.putString("access_token", accessToken);
        editor.putString("role", role);
        editor.putBoolean("is_logged_in", true);
        
        // 提交修改
        editor.apply();
        
        Log.d(TAG, "用户信息已保存: " + username + ", 角色: " + role);
    }

    public void back(View V){
        finish();
    }
}