package cn.edu.zime.tjh.iotapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import cn.edu.zime.tjh.iotapp.api.ApiCallback;
import cn.edu.zime.tjh.iotapp.api.ApiService;

public class LightActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "LightActivity";
    private Sensor lightSensor;
    private SensorManager sensorManager;
    private LineChart lightLineChart; // 曲线图组件
    private float[] values; // 传感器的数值数组
    private static final int RANGE_LIMIT = 20; // 曲线图只显示20个点
    private TextView lightValueText;
    private TableLayout lightTable;
    private int dataIndex = 0;
    private ApiService apiService;
    private static final long UPLOAD_INTERVAL = 5000; // 数据上传间隔，5秒
    private long lastUploadTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        apiService = ApiService.getInstance();
        initUI();
    }

    private void initUI() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            Toast.makeText(this, "光照传感器不可用", Toast.LENGTH_SHORT).show();
        }
        lightLineChart = findViewById(R.id.lineChart);
        lightValueText = findViewById(R.id.lightValueText);
        lightTable = findViewById(R.id.lightTable);
        setupChart();
    }

    private void setupChart() {
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), "光照");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(android.R.color.black);
        LineData lineData = new LineData(dataSet);
        lightLineChart.setData(lineData);
        lightLineChart.invalidate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            float value = sensorEvent.values[0];
            Log.d(TAG, "onSensorChanged: " + value);
            updateTextDisplay(value);
            updateTableDisplay(value);
            showChart(value);
            
            // 检查是否需要上传数据
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUploadTime >= UPLOAD_INTERVAL) {
                uploadLightData(value);
                lastUploadTime = currentTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // 传感器精度变化时的处理
    }

    private void updateTextDisplay(float value) {
        lightValueText.setText("光照强度: " + value);
    }

    private void updateTableDisplay(float value) {
        TableRow row = new TableRow(this);
        TextView indexTextView = new TextView(this);
        indexTextView.setText(String.valueOf(dataIndex++));
        indexTextView.setPadding(8, 8, 8, 8);
        row.addView(indexTextView);

        TextView valueTextView = new TextView(this);
        valueTextView.setText(String.valueOf(value));
        valueTextView.setPadding(8, 8, 8, 8);
        row.addView(valueTextView);

        lightTable.addView(row);
    }

    private void showChart(float value) {
        if (values == null) {
            values = new float[RANGE_LIMIT];
        }
        for (int i = 0; i < RANGE_LIMIT - 1; i++) {
            values[i] = values[i + 1];
        }
        values[RANGE_LIMIT - 1] = value;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < RANGE_LIMIT; i++) {
            entries.add(new Entry(i, values[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "光照");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        LineData lineData = new LineData(dataSet);
        lightLineChart.setData(lineData);
        lightLineChart.invalidate();
    }

    /**
     * 上传光照数据到服务器
     */
    private void uploadLightData(float value) {
        try {
            JSONObject data = new JSONObject();
            data.put("sensor_type", "light");
            data.put("sensor_value", value);
            data.put("alarm_level", getLightLevel(value));

            apiService.submitSensorData(data, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d(TAG, "光照数据上传成功: " + response.toString());
                }

                @Override
                public void onFailure(String errorMsg) {
                    Log.e(TAG, "光照数据上传失败: " + errorMsg);
                    runOnUiThread(() -> {
                        Toast.makeText(LightActivity.this, 
                            "数据上传失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "准备光照数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据光照值判断光照等级
     * 返回与数据库枚举类型匹配的值: 'Normal', 'Reminder', 'Warning', 'Critical'
     */
    private String getLightLevel(float value) {
        if (value < 50) {
            return "Critical";     // 光照极低，需要立即处理
        } else if (value < 100) {
            return "Warning";      // 光照不足，需要注意
        } else if (value > 2000) {
            return "Warning";      // 光照过强，需要注意
        } else if (value > 1500) {
            return "Reminder";     // 光照偏强，建议关注
        } else {
            return "Normal";       // 光照正常
        }
    }

    public void back(View V){
        finish();
    }
}