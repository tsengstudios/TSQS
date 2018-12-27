package me.tseng.studios.tchores.java.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.NotificationPublisher;
import me.tseng.studios.tchores.java.RestaurantAddActivity;

public class AlarmManagerUtil {

    // This value is defined and consumed by app code, so any value will work.
    // There's no significance to this sample using 0.
    public static final int REQUEST_CODE = 0;

    public static void setAlarm(Context context, Intent intent, String sAlarmLocalDateTime) {

        // BEGIN_INCLUDE (pending_intent_for_alarm)
        // Because the intent must be fired by a system service from outside the application,
        // it's necessary to wrap it in a PendingIntent.  Providing a different process with
        // a PendingIntent gives that other process permission to fire the intent that this
        // application has created.
        // Also, this code creates a PendingIntent to start an Activity.  To create a
        // BroadcastIntent instead, simply call getBroadcast instead of getIntent.

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, getNotification(context, "THIS IS THE CHORE TEXT TO FILL IN"));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // OLD PendingIntent below  TODO: remove or repurpose intent from function parameter.  Perhaps it will be the intent to Log the chore action
        // PendingIntent pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, 0);

        // Calculate alarm time in Epoch milliseconds
        int alarmType = AlarmManager.RTC_WAKEUP;
        long rtcAlarmMillis = ZonedDateTime.of(LocalDateTime.parse(sAlarmLocalDateTime),ZoneId.systemDefault()).toEpochSecond()*1000;

        Log.i("RTC Alarm","rtcAlarmMillis     = " + String.valueOf(rtcAlarmMillis));
        Log.i("RTC Alarm","currentTimeMillis  = " + String.valueOf(System.currentTimeMillis()));
        Log.i("RTC Alarm","LocalDateTime now  = " + LocalDateTime.now().toString());

        if (rtcAlarmMillis < System.currentTimeMillis())
            return;     // don't set alarms to the past

        // The AlarmManager, like most system services, isn't created by application code, but
        // requested from the system.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(alarmType,rtcAlarmMillis, pendingIntent);

        // END_INCLUDE (configure_alarm_manager);

        Log.i("RTC Alarm","Alarm set.");
    }


    private static Notification getNotification(Context context, String content) {
        Notification.Builder builder = new Notification.Builder(context,"");
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_monetization_on_white_24px);
        return builder.build();
    }

}
