package com.example.tassadar.senzory.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tassadar.senzory.BaseSensor;
import com.example.tassadar.senzory.R;

public class SpiritLevel extends BaseSensor implements SensorEventListener {
    @Override
    public int[] getRequiredSensors() {
        return new int[] { Sensor.TYPE_GRAVITY };
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_spiritlevel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return new SpiritLevelView(inflater.getContext());
    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onSensorChanged(SensorEvent event) {
        SpiritLevelView level = (SpiritLevelView)getView();
        if (level == null) {
            return;
        }

        float ax = event.values[0] / SensorManager.STANDARD_GRAVITY;
        float ay = event.values[1] / SensorManager.STANDARD_GRAVITY;
        float az = event.values[2] / SensorManager.STANDARD_GRAVITY;
        double yaw = Math.atan(ay / Math.sqrt(ax*ax + az*az));
        double pitch = Math.atan(az / Math.sqrt(ax*ax + ay*ay));
        double roll = Math.atan(ax / Math.sqrt(ay*ay + az*az));

        level.setOrientation((float)yaw, (float)pitch, (float)roll);
    }

    public void onSensorResume() {
        Sensor grav = mSensorMgr.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorMgr.registerListener(this, grav, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onSensorPause() {
        mSensorMgr.unregisterListener(this);
    }
}
