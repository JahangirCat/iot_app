package cn.edu.zime.tjh.iotapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String username; // 用户名
    private String accessToken; // 用于存储 access_token
    private String role; // 用于存储用户角色
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // 从SharedPreferences中获取用户信息
        loadUserInfo();

        // 为 mystate 按钮设置点击事件
        findViewById(R.id.mystate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 MystateActivity
                Intent mystateIntent = new Intent(MainActivity.this, MystateActivity.class);
                startActivity(mystateIntent);
            }
        });
    }
    
    /**
     * 从SharedPreferences加载用户信息
     */
    private void loadUserInfo() {
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);
        
        if (isLoggedIn) {
            username = preferences.getString("username", "");
            accessToken = preferences.getString("access_token", "");
            role = preferences.getString("role", "");
            
            // 显示欢迎信息
            if (!username.isEmpty()) {
                Toast.makeText(this, "欢迎回来, " + username, Toast.LENGTH_SHORT).show();
            }
        } else {
            // 如果用户未登录，跳转到登录界面
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // 关闭当前界面
        }
    }

    public void equipment(View V){
        Intent intent = new Intent(MainActivity.this, AddEquipment.class);
        startActivity(intent);
    }

    public void Alarm(View V){
        Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
        startActivity(intent);
    }
    public void Light(View V){
        Intent intent = new Intent(MainActivity.this, LightActivity.class);
        startActivity(intent);
    }
    
    public void Altiduede(View V){
        Intent intent = new Intent(MainActivity.this, AltitudeActivity.class);
        startActivity(intent);
    }

    public void Location(View V){
        Intent intent = new Intent(MainActivity.this, LocationActivity.class);
        startActivity(intent);
    }

    public void Scan(View V){
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent);
    }

    /**
     * 启动YOLO物体检测功能
     * 此方法用于处理activity_host.xml中的按钮点击事件
     */
    public void YOLO(View view) {
        Intent intent = new Intent(this, YOLO.class);
        startActivity(intent);
    }

    public void Intercation(View view) {
        Intent intent = new Intent(MainActivity.this, InteractionActivity.class);
        startActivity(intent);
    }
    public void Presure(View V){
        Intent intent = new Intent(MainActivity.this, PressureActivity.class);
        startActivity(intent);
    }
}
