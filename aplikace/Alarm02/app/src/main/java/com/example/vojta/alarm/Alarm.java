package com.example.vojta.alarm;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Alarm implements Serializable, Comparable<Alarm> {
    public static final int HOUR_ANY = -1;

    int hour;
    int minute;
    String soundUri;
    boolean enabled;
    boolean vibrate;
    boolean[] repeatDays;

    public Alarm() {
        repeatDays = new boolean[7];
        for(int i = 0; i < repeatDays.length; ++i) {
            repeatDays[i] = true;
        }
    }

    public String getTimeText() {
        if(hour == HOUR_ANY) {
            return String.format("--:%02d", minute);
        } else {
            return String.format("%02d:%02d", hour, minute);
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

        int cur_day = c.get(Calendar.DAY_OF_WEEK) - 1;
        boolean found = false;
        for(int i = cur_day; i < cur_day+7; ++i) {
            if(repeatDays[i%repeatDays.length])
                break;
            found = true;
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        if(!found)
            return Long.MAX_VALUE;

        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if(hour != HOUR_ANY)
            c.set(Calendar.HOUR_OF_DAY, hour);
        return c.getTimeInMillis();
    }

    public int compareTo(Alarm o) {
        if(hour != o.hour)
            return hour - o.hour;
        return minute - o.minute;
    }

    public List<Integer> getSelectedDays() {
        ArrayList<Integer> res = new ArrayList<>(repeatDays.length);
        for (int i = 0; i < repeatDays.length; ++i) {
            if (repeatDays[i])
                res.add(i + 1);
        }
        return res;
    }


    private static void loadAlarms(Context ctx, List<Alarm> alarms) {
        FileInputStream fin = null;
        ObjectInputStream oin = null;
        try {
            fin = ctx.openFileInput("alarms.dat");
            oin = new ObjectInputStream(fin);

            alarms.clear();

            final int count = oin.readInt();
            for(int i = 0; i < count; ++i) {
                Alarm a = (Alarm)oin.readObject();
                alarms.add(a);
            }
        } catch(FileNotFoundException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try { oin.close(); } catch (Exception e) { }
            try { fin.close(); } catch (Exception e) { }
        }
    }

    private static List<Alarm> mAlarms = null;
    private static final Object mAlarmsLock = new Object();
    public static List<Alarm> getAlarms(Context ctx) {
        synchronized (mAlarmsLock) {
            if(mAlarms == null) {
                mAlarms = new ArrayList<>();
                loadAlarms(ctx, mAlarms);
            }
            return mAlarms;
        }
    }

    public static void saveAlarms(Context ctx) {
        synchronized (mAlarmsLock) {
            if(mAlarms == null)
                return;

            FileOutputStream fout = null;
            ObjectOutputStream oout = null;
            try {
                fout = ctx.openFileOutput("alarms.dat", 0);
                oout = new ObjectOutputStream(fout);
                oout.writeInt(mAlarms.size());
                for(Alarm a : mAlarms)
                    oout.writeObject(a);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { oout.close(); } catch (Exception e) { }
                try { fout.close(); } catch (Exception e) { }
            }
        }
    }

}
