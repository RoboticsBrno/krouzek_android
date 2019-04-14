package com.example.vojta.alarm;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.media.RingtoneManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, AlarmAdapter.OnAlarmChangedListener {
    private AlarmAdapter mAdapter;
    private List<Alarm> mAlarms;
    private boolean mAlarmsModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAlarms = Alarm.getAlarms(this);
        mAdapter = new AlarmAdapter(mAlarms, this);

        RecyclerView list = findViewById(R.id.alarmList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(mAdapter);

        AlarmScheduler.schedule(this, mAlarms);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_alarm:
                showTimePicker();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onPause() {
        super.onPause();
        if(mAlarmsModified) {
            mAlarmsModified = false;
            Alarm.saveAlarms(this);
        }
    }

    private void onAlarmModified() {
        Collections.sort(mAlarms);
        mAlarmsModified = true;
        AlarmScheduler.schedule(this, mAlarms);
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, this, hour, minute,
                DateFormat.is24HourFormat(this));
        dialog.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        Alarm a = new Alarm();
        a.hour = hourOfDay;
        a.minute = minute;
        a.enabled = true;
        a.vibrate = true;
        a.soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
        mAlarms.add(a);
        onAlarmModified();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAlarmChanged(Alarm a) {
        onAlarmModified();
    }
}
