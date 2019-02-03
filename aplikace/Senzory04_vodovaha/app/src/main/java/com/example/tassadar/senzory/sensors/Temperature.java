package com.example.tassadar.senzory.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tassadar.senzory.BaseSensor;
import com.example.tassadar.senzory.R;

public class Temperature extends BaseSensor implements SensorEventListener {
    private boolean mBatteryRegistered;

    @Override
    public int[] getRequiredSensors() {
        return new int[] { };
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_thermo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sensor_temp, container, false);
    }

    private void setTemp(float temp, String source) {
        View root = getView();
        if (root == null) {
            return;
        }

        TextView tempView = root.findViewById(R.id.temp);
        tempView.setText(String.format("%.1f °C", temp));

        TextView sourceView = root.findViewById(R.id.temp_source);
        sourceView.setText(source);
    }

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            setTemp(temp / 10, "battery");
        }
    };

    public void onSensorChanged(SensorEvent sensorEvent) {
        View root = getView();
        if (root == null) {
            return;
        }

        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                setTemp(sensorEvent.values[0], "ambient");
                break;
            case Sensor.TYPE_PRESSURE:
                TextView pressure = root.findViewById(R.id.pressure);
                pressure.setText(String.format("%.0f hPa", sensorEvent.values[0]));
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                TextView humidity = root.findViewById(R.id.humidity);
                humidity.setText(String.format("%.1f%%", sensorEvent.values[0]));
                break;
            case Sensor.TYPE_LIGHT:
                TextView light = root.findViewById(R.id.light);
                light.setText(String.format("%.0f lux", sensorEvent.values[0]));

                final int MAX_LIGHT = 10000;
                double val = Math.min(MAX_LIGHT, sensorEvent.values[0]);
                val = Math.log(val) / Math.log(MAX_LIGHT);
                int rgb = (int)(val * 255);
                root.setBackgroundColor(Color.rgb(rgb, rgb, rgb));
                break;
        }
    }

    public void onSensorResume() {
        Sensor temp = mSensorMgr.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if(temp != null) {
            mSensorMgr.registerListener(this, temp, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            this.getContext().registerReceiver(mBatteryReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            mBatteryRegistered = true;
        }

        final int[] types = new int[] {
                Sensor.TYPE_PRESSURE,
                Sensor.TYPE_RELATIVE_HUMIDITY,
                Sensor.TYPE_LIGHT,
        };

        for(int typ : types) {
            Sensor s = mSensorMgr.getDefaultSensor(typ);
            if(s != null) {
                mSensorMgr.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void onSensorPause() {
        mSensorMgr.unregisterListener(this);
        if(mBatteryRegistered) {
            getContext().unregisterReceiver(mBatteryReceiver);
            mBatteryRegistered = false;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
