package com.example.vojta.alarm;

import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Alarm implements Serializable {
    public static final int HOUR_ANY = -1;

    int hour;
    int minute;
    String soundUri;
    boolean enabled;
    boolean vibrate;

    public String getTimeText() {
        if(hour == HOUR_ANY) {
            return "-:" + minute;
        } else {
            return hour + ":" + minute;
        }
    }

    public long getNextTime() {
        Calendar c = Calendar.getInstance();
        if(hour != HOUR_ANY) {
            int ch = c.get(Calendar.HOUR_OF_DAY);
            if(ch > hour || (ch == hour && c.get(Calendar.MINUTE) >= minute))
                c.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            if(c.get(Calendar.MINUTE) >= minute)
                c.add(Calendar.HOUR_OF_DAY, 1);
        }
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if(hour != HOUR_ANY)
            c.set(Calendar.HOUR_OF_DAY, hour);
        return c.getTimeInMillis();
    }

    public static List<Alarm> getAlarms() {
        ArrayList<Alarm> res = new ArrayList<>();

        // Temporary
        Alarm a = new Alarm();
        a.hour = HOUR_ANY;
        a.minute = 14;
        a.enabled = true;
        a.vibrate = true;
        a.soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
        res.add(a);
        return res;
    }
}
