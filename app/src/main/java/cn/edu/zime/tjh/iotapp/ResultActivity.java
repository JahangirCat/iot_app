package cn.edu.zime.tjh.iotapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvResult;
    private Button btnOpenLink;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvResult = findViewById(R.id.tv_result);
        btnOpenLink = findViewById(R.id.btn_open_link);

        // 获取传递过来的扫描结果
        Intent intent = getIntent();
        String scanResult = intent.getStringExtra("SCAN_RESULT");

        if (scanResult != null && !scanResult.isEmpty()) {
            tvResult.setText(scanResult);
            // 自动检测并高亮链接
            Linkify.addLinks(tvResult, Linkify.WEB_URLS);
            // 设置文本的可点击性
            tvResult.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        } else {
            tvResult.setText("未检测到有效的扫描结果");
        }
    }

    // 简单的 URL 验证
    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    // 返回按钮点击事件
    public void ScanResultBack(View v) {
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}