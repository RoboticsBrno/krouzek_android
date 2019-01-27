package com.example.tassadar.senzory.sensors;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tassadar.senzory.BaseSensor;
import com.example.tassadar.senzory.R;
import com.tasssadar.voicerecordingvisualizer.RecordingSampler;
import com.tasssadar.voicerecordingvisualizer.VisualizerView;

public class Sound extends BaseSensor implements RecordingSampler.CalculateVolumeListener {
    @Override
    public int[] getRequiredSensors() {
        return new int[] { };
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_microphone;
    }

    RecordingSampler mSampler;
    VisualizerView mVisualizer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sensor_sound, container, false);
        mVisualizer = root.findViewById(R.id.visualizer);
        return root;
    }

    public void onSensorResume() {
        int p = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
        if (p != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},0);
            return;
        }

        mSampler = new RecordingSampler();
        mSampler.setSamplingInterval(10);
        mSampler.setVolumeListener(this);
        mSampler.link(mVisualizer);
        mSampler.startRecording();
    }

    public void onSensorPause() {
        if(mSampler != null) {
            mSampler.stopRecording();
            mSampler.release();
            mSampler = null;
        }
    }

    @Override
    public void onCalculateVolume(final int volume) {
        Activity act = getActivity();
        if(act == null)
            return;

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View root = getView();
                if(root != null) {
                    TextView sound = root.findViewById(R.id.sound);
                    sound.setText(volume + " dB");
                }
            }
        });
    }
}
