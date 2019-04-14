package com.example.vojta.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;
import java.util.List;

public class AlarmScheduler {
    public static final int NOTIFICATION_ID = 1;

    public static boolean schedule(Context ctx, List<Alarm> alarms) {
        Alarm earliest = null;
        long earliestTm = 0;
        for(Alarm a : alarms) {
            if(!a.enabled) {
                continue;
            }

            long tm = a.getNextTime();
            Date d = new Date(tm);
            Log.i("SChedule", d.toString());
            if(earliest == null || earliestTm > tm) {
                earliestTm = tm;
                earliest = a;
            }
        }

        Intent i = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager man = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        man.cancel(pi);

        if(earliest == null)
            return false;

        Bundle shell = new Bundle();
        shell.putSerializable("alarm", earliest);

        i.putExtra("shell", shell);
        pi = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            man.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, earliestTm, pi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            man.setExact(AlarmManager.RTC_WAKEUP, earliestTm, pi);
        } else {
            man.set(AlarmManager.RTC_WAKEUP, earliestTm, pi);
        }
        return true;
    }
}
