package com.example.tassadar.senzory;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BaseSensor extends Fragment {
    protected SensorManager mSensorMgr;
    private boolean mPaused = true;
    private boolean mHidden = true;

    protected void onSensorResume() { }
    protected void onSensorPause() { }

    private void updateSensorState(boolean paused, boolean hidden) {
        boolean oldState = !mHidden && !mPaused;
        boolean newState = !hidden && !paused;
        mPaused = paused;
        mHidden = hidden;

        if(oldState == newState)
            return;

        if(newState)
            this.onSensorResume();
        else
            this.onSensorPause();
    }

    public void onResume() {
        super.onResume();
        mSensorMgr = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);
        updateSensorState(false, mHidden);
    }

    public void onPause() {
        super.onPause();
        updateSensorState(true, mHidden);
        mSensorMgr = null;
    }

    public void onHiddenChanged(boolean hidden) {
        updateSensorState(mPaused, hidden);
    }

    public abstract int[] getRequiredSensors();
    public abstract int getIcon();
}
