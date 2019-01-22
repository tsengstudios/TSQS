package me.tseng.studios.tchores.java;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// TODO rename NotificationPublisher to ActOnAlarmBR
public class NotificationPublisher extends BroadcastReceiver {

    private static final String TAG = "TChores.NotificationPublisher";

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        String id = intent.getStringExtra(NOTIFICATION_ID);
        notificationManager.notify(id.hashCode(), notification);    // hashCode won't guarantee uniqueness, but probably for two alarms at the same time?
        Log.i(TAG, "Fire notification id = " + id.hashCode());

    }

}


