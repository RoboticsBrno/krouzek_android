package com.example.vojta.alarm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private AlarmAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new AlarmAdapter(Alarm.getAlarms());

        RecyclerView list = findViewById(R.id.alarmList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(mAdapter);

        AlarmScheduler.schedule(this, Alarm.getAlarms());
    }
}
