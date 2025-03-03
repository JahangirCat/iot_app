package cn.edu.zime.tjh.iotapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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

public class AltitudeActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor altitudeSensor;
    private LineChart altitudeChart;
    private TextView altitudeValueText;
    private TableLayout altitudeTable;
    private List<Entry> entries = new ArrayList<>();
    private int entryIndex = 0;
    private int tableIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_altitude);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        altitudeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        if (altitudeSensor == null) {
            Toast.makeText(this, "海拔传感器（压力传感器）不可用", Toast.LENGTH_SHORT).show();
        }

        altitudeChart = findViewById(R.id.altitudeChart);
        altitudeValueText = findViewById(R.id.altitudeValueText);
        altitudeTable = findViewById(R.id.altitudeTable);

        setupChart();
    }

    private void setupChart() {
        LineDataSet dataSet = new LineDataSet(entries, "海拔");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(android.R.color.black);
        LineData lineData = new LineData(dataSet);
        altitudeChart.setData(lineData);
        altitudeChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (altitudeSensor != null) {
            sensorManager.registerListener(this, altitudeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float pressure = event.values[0];
            float altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);

            altitudeValueText.setText("海拔: " + altitude + " 米");

            entries.add(new Entry(entryIndex++, altitude));
            if (entries.size() > 20) {
                entries.remove(0);
            }

            LineData lineData = altitudeChart.getData();
            LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(0);
            dataSet.setValues(entries);
            lineData.notifyDataChanged();
            altitudeChart.notifyDataSetChanged();
            altitudeChart.invalidate();

            addDataToTable(altitude);
        }
    }

    private void addDataToTable(float altitude) {
        TableRow row = new TableRow(this);
        TextView indexTextView = new TextView(this);
        indexTextView.setText(String.valueOf(tableIndex++));
        indexTextView.setPadding(8, 8, 8, 8);
        row.addView(indexTextView);

        TextView valueTextView = new TextView(this);
        valueTextView.setText(String.valueOf(altitude));
        valueTextView.setPadding(8, 8, 8, 8);
        row.addView(valueTextView);

        altitudeTable.addView(row);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 传感器精度变化时的处理
    }
    public void back(View V){
        finish();
    }
}