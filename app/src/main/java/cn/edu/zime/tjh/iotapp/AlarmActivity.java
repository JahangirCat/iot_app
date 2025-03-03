package cn.edu.zime.tjh.iotapp;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView accelerationText;
    private View mainLayout;
    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // 初始化传感器管理器
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 获取加速度传感器
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 初始化 UI 组件
        accelerationText = findViewById(R.id.accelerationText);
        mainLayout = findViewById(R.id.mainLayout);

        // 初始化警报器
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册传感器监听器
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 取消传感器监听器注册
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // 获取加速度值
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // 计算加速度的大小
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            // 更新 UI 显示加速度值
            accelerationText.setText("加速度,: " + String.format("%.2f", acceleration) + " m/s²");

            // 检查加速度是否大于 6 m/s²
            if (acceleration > 6) {
                // 屏幕变红
                mainLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                // 发出警报
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            } else {
                // 恢复默认背景颜色
                mainLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 传感器精度改变时的处理，这里不需要做任何操作
    }
    public void back(View V){
        finish();
    }
}