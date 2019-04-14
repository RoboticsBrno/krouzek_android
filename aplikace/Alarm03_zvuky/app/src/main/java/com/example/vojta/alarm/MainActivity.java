package com.example.vojta.alarm;

import android.Manifest;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    private static final int REQ_RINGTONE = 0;
    private static final int REQ_SOUND_FILE = 1;

    private AlarmAdapter mAdapter;
    private List<Alarm> mAlarms;
    private boolean mAlarmsModified;
    private int mAlarmIdxSound;

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

    @Override
    public void pickRingtone(Alarm a, int position) {
        mAlarmIdxSound = position;

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select tone for the Alarm:");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(a.soundUri));
        startActivityForResult(intent, REQ_RINGTONE);
    }
    @Override
    public void pickSoundFile(Alarm a, int position) {
        mAlarmIdxSound = position;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    0);
            return;
        }


        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        startActivityForResult(intent, REQ_SOUND_FILE);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickSoundFile(mAlarms.get(mAlarmIdxSound), mAlarmIdxSound);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK)
            return;

        Alarm a = mAlarms.get(mAlarmIdxSound);
        Uri uri = null;
        if(requestCode == REQ_RINGTONE) {
            uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        } else if(requestCode == REQ_SOUND_FILE) {
            uri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        }

        if (uri != null) {
            a.soundUri = uri.toString();
            mAdapter.notifyItemChanged(mAlarmIdxSound);
            onAlarmModified();
        }
    }
}
