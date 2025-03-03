package cn.edu.zime.tjh.iotapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MystateActivity extends AppCompatActivity {
    private TextView usernameTextView; // 用于显示用户名
    private TextView roleTextView; // 用于显示角色
    private TextView stateName; // 用于显示状态名称
    private Button btnLogout; // 退出登录按钮
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mystate);

        // 初始化视图
        usernameTextView = findViewById(R.id.Username);
        roleTextView = findViewById(R.id.role);
        stateName = findViewById(R.id.stateName);
        btnLogout = findViewById(R.id.btnBack);

        // 从SharedPreferences中获取保存的用户信息
        loadUserInfo();

        // 设置退出登录按钮点击事件
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    /**
     * 从SharedPreferences中加载用户信息
     */
    private void loadUserInfo() {
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String accessToken = preferences.getString("access_token", "");
        String role = preferences.getString("role", "");

        if (!username.isEmpty()) {
            // 设置用户名
            stateName.setText(username);
            usernameTextView.setText(username);
            
            // 设置角色
            roleTextView.setText(role);
        } else {
            // 如果没有保存的用户信息，跳转到登录界面
            Toast.makeText(this, "您尚未登录，请先登录", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            finish();
        }
    }

    /**
     * 退出登录
     */
    private void logout() {
        // 清除SharedPreferences中的用户数据
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // 显示退出成功消息
        Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();

        // 跳转到登录界面
        navigateToLogin();
        
        // 关闭当前界面
        finish();
    }

    /**
     * 跳转到登录界面
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        // 清除任务栈中的其他活动，使登录界面成为新的栈顶
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * 后退方法，由XML中的onClick属性调用
     */
    public void back(View view) {
        // 此方法会被XML中的onClick="back"调用
        logout();
    }
}

