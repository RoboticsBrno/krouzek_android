package com.example.tassadar.pocasi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Date;
import java.util.Locale;

public class WidgetService extends Service implements GetForecastTask.OnForecastLoadedListener {
    private static final String CHANNEL_ID = "widgetupdate";
    private int mStartId;
    private int[] mWidgetIds;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Widget updates", NotificationManager.IMPORTANCE_MIN);
            channel.setDescription("Widget forecast update is happening.");
            NotificationManager man = getSystemService(NotificationManager.class);
            man.createNotificationChannel(channel);

            Notification.Builder b = new Notification.Builder(this, CHANNEL_ID);
            b.setContentTitle(getString(R.string.app_name));
            b.setContentText("Updating forecast");
            b.setAutoCancel(true);
            startForeground(1, b.build());
        }

        mStartId = startId;
        mWidgetIds = intent.getIntArrayExtra("appWidgetIds");

        Log.i("WidgetService", "start");

        SharedPreferences pref = getSharedPreferences("", MODE_PRIVATE);
        if(!pref.contains("latitude")) {
            stopSelf(mStartId);
            return START_NOT_STICKY;
        }

        double latitude = pref.getFloat("latitude", 0);
        double longitude = pref.getFloat("longitude", 0);

        String url = String.format(Locale.US, Api.URL_TEMPLATE, latitude, longitude);
        new GetForecastTask(this).execute(url);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onForecastLoaded(String forecastJson) {
        stopSelf(mStartId);

        if (forecastJson == null) {
            Log.e("WidgetService", "Nepodařilo se načíst počasí!");
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferences("", MODE_PRIVATE).edit();
        editor.putString("forecastJson", forecastJson);
        editor.putLong("forecastTime", new Date().getTime());
        editor.apply();

        Log.i("WidgetService", "Počasí stáhnuto");

        Intent i = new Intent(this, WidgetReceiver.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mWidgetIds);
        sendBroadcast(i);
    }
}
