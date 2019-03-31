package com.example.vojta.alarm;

import android.app.ActionBar;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {
    private TextView mTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        getSupportActionBar().hide();

        Button btn = findViewById(R.id.dismissBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManagerCompat man = NotificationManagerCompat.from(AlarmActivity.this);
                man.cancel(AlarmScheduler.NOTIFICATION_ID);
                finish();
            }
        });

        mTimeView = findViewById(R.id.time);
        updateTime();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void updateTime() {
        Calendar calc = Calendar.getInstance();
        mTimeView.setText(String.format("%d:%02d",
                calc.get(Calendar.HOUR_OF_DAY), calc.get(Calendar.MINUTE)));

        mTimeView.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
            }
        }, 60000 - calc.get(Calendar.SECOND)*1000);
    }
}
