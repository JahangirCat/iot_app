package cn.edu.zime.tjh.iotapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

public class ScanActivity extends AppCompatActivity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan); // 自定义布局

        barcodeScannerView = findViewById(R.id.dbv_custom);
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);

        // 设置扫码回调
        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                String scanResult = result.getText();
                if (scanResult != null && !scanResult.isEmpty()) {
                    // 启动 ResultActivity 并传递扫描结果
                    Intent intent = new Intent(ScanActivity.this, ResultActivity.class);
                    intent.putExtra("SCAN_RESULT", scanResult);
                    startActivity(intent);
                    finish(); // 可选：扫描完成后关闭当前 Activity
                }
            }
        });

        // 开始扫码
        barcodeScannerView.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }
}