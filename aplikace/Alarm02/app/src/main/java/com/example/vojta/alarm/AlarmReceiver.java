package com.example.vojta.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "alarm_notify";

    private String createNotificationChannel(Context ctx, Uri sound, boolean vibrate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return "";
        }

        NotificationManager man = ctx.getSystemService(NotificationManager.class);
        long channelIdIdx = 0;
        for(NotificationChannel c : man.getNotificationChannels()) {
            String n = c.getId();
            if(n.startsWith(CHANNEL_ID)) {
                long idx = Long.parseLong(n.substring(CHANNEL_ID.length()));
                channelIdIdx = idx + 1;
                man.deleteNotificationChannel(n);
                break;
            }
        }

        String channelId = CHANNEL_ID + channelIdIdx;

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, "Alarms", importance);
        channel.setDescription("Triggered alarms");
        channel.enableVibration(vibrate);
        if(sound != null) {
            AudioAttributes.Builder b = new AudioAttributes.Builder();
            b.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN);
            b.setLegacyStreamType(AudioManager.STREAM_ALARM);
            b.setUsage(AudioAttributes.USAGE_ALARM);
            channel.setSound(sound, b.build());
        }
        man.createNotificationChannel(channel);
        return channelId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle shell = intent.getBundleExtra("shell");
        Alarm a = (Alarm) shell.getSerializable("alarm");

        AlarmScheduler.schedule(context, Alarm.getAlarms(context));

        Uri sound = null;
        if(a.soundUri != null) {
            sound = Uri.parse(a.soundUri);
        }

        String channelId = createNotificationChannel(context, sound, a.vibrate);

        Intent fullscreen = new Intent(context, AlarmActivity.class);
        fullscreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pi = PendingIntent.getActivity(context, 0, fullscreen,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setSmallIcon(R.drawable.ic_alarm)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle("Alarm")
                .setContentText(a.getTimeText())
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setFullScreenIntent(pi, true);

        if(a.vibrate) {
            builder.setVibrate(new long[] { 1000 });
        }

        if(sound != null) {
            builder.setSound(sound, AudioManager.STREAM_ALARM);
        }

        Notification n = builder.build();
        n.flags |= Notification.FLAG_INSISTENT;

        NotificationManagerCompat man = NotificationManagerCompat.from(context);
        man.notify(AlarmScheduler.NOTIFICATION_ID, n);
    }

}
