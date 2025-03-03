package cn.edu.zime.tjh.iotapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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

import java.util.ArrayList;
import java.util.List;

public class PressureActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {

    private Sensor pressureSensor;
    private SensorManager sensorManager;
    private LineChart pressureLineChart; // 曲线图组件
    private float[] values; // 传感器的数值数组
    private static final int RANGE_LIMIT = 20; // 曲线图只显示20个点
    private TextView pressureValueText;
    private TableLayout pressureTable;
    private int dataIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressure);
        initUI();
    }

    private void initUI() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (!isPressureSensorSupported()) {
            Toast.makeText(this, "压力传感器不可用，将使用触摸模拟", Toast.LENGTH_SHORT).show();
            setupTouchListener(); // 切换到触摸模拟模式
        } else {
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        pressureLineChart = findViewById(R.id.lineChart);
        pressureValueText = findViewById(R.id.pressureValueText);
        pressureTable = findViewById(R.id.pressureTable);
        setupChart();
    }

    private void setupTouchListener() {
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float touchSize = event.getSize(); // 获取触摸面积
        float touchMajor = event.getTouchMajor(); // 获取触摸椭圆的长轴长度
        float pressure = event.getPressure(); // 获取触摸压力值

        // 将触摸面积或压力值映射到更大的范围（例如 0-100）
        float mappedValue = touchSize * 100; // 或者使用 touchMajor 或 pressure

        Log.d(PressureActivity.class.getName(), "onTouch - Touch Size: " + touchSize + ", Touch Major: " + touchMajor + ", Pressure: " + pressure);

        Log.d("TouchEvent", "Touch Size: " + touchSize);
        Log.d("TouchEvent", "Touch Major: " + touchMajor);
        Log.d("TouchEvent", "Pressure: " + pressure);
        // 更新显示
        updateTextDisplay(mappedValue);
        updateTableDisplay(mappedValue);
        showChart(mappedValue);

        return true; // 消费触摸事件
    }

    private void setupChart() {
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), "压力");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(android.R.color.black);
        LineData lineData = new LineData(dataSet);
        pressureLineChart.setData(lineData);
        pressureLineChart.invalidate();
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
        if (sensorManager != null && pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float value = sensorEvent.values[0];
            Log.d(PressureActivity.class.getName(), "onSensorChanged: " + value);
            updateTextDisplay(value);
            updateTableDisplay(value);
            showChart(value);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // 传感器精度变化时的处理
    }

    private void updateTextDisplay(float value) {
        pressureValueText.setText("压力值: " + value + " hPa");
    }

    private void updateTableDisplay(float value) {
        // 如果表格行数超过限制，移除最旧的行
        if (pressureTable.getChildCount() >= RANGE_LIMIT) {
            pressureTable.removeViewAt(0);
        }

        TableRow row = new TableRow(this);
        TextView indexTextView = new TextView(this);
        indexTextView.setText(String.valueOf(dataIndex++));
        indexTextView.setPadding(8, 8, 8, 8);
        row.addView(indexTextView);

        TextView valueTextView = new TextView(this);
        valueTextView.setText(String.valueOf(value));
        valueTextView.setPadding(8, 8, 8, 8);
        row.addView(valueTextView);

        pressureTable.addView(row);
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

        LineDataSet dataSet = new LineDataSet(entries, "电容变化");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        LineData lineData = new LineData(dataSet);
        pressureLineChart.setData(lineData);
        pressureLineChart.invalidate();
    }

    public void back(View V) {
        finish();
    }
    private boolean isPressureSensorSupported() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        return sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}