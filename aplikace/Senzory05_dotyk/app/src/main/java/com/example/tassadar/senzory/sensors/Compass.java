package com.example.tassadar.senzory.sensors;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tassadar.senzory.R;
import com.example.tassadar.senzory.BaseSensor;


import java.util.Arrays;

public class Compass extends BaseSensor implements SensorEventListener {
    public int[] getRequiredSensors() {
        return new int[] { Sensor.TYPE_ROTATION_VECTOR };
    }

    public int getIcon() {
        return R.drawable.ic_compass;
    }

    private float[] mRotation = new float[16];
    private float[] mOrientation = new float[3];
    private float[] mSamples = new float[11];
    private int mSampleIdx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sensor_compass, container, false);
    }

    public void onSensorResume() {
        Sensor rotation = mSensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorMgr.registerListener(this, rotation, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onSensorPause() {
        mSensorMgr.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        View root = getView();
        if(root == null) {
            return;
        }

        SensorManager.getRotationMatrixFromVector(mRotation, sensorEvent.values);
        SensorManager.getOrientation(mRotation, mOrientation);

        float azimut = mOrientation[0] * (float)(180.0 / Math.PI);
        mSamples[mSampleIdx++] = azimut;
        if(mSampleIdx < mSamples.length)
            return;
        mSampleIdx = 0;

        Arrays.sort(mSamples);
        azimut = mSamples[mSamples.length/2];

        ImageView compass = root.findViewById(R.id.compass);
        compass.setRotation(-azimut);

        int dispAzimut = Math.round(azimut >= 0 ? azimut : 360 + azimut);
        TextView azimutText = root.findViewById(R.id.azimut);
        azimutText.setText(String.format(" %d°", dispAzimut));
    }
}
