package com.example.tassadar.senzory.sensors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tassadar.senzory.BaseSensor;
import com.example.tassadar.senzory.R;

public class Touch extends BaseSensor {
    @Override
    public int[] getRequiredSensors() {
        return new int[0];
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_touch;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return new TouchView(inflater.getContext());
    }
}
