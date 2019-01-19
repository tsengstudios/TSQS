package me.tseng.studios.tchores.java.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
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
    private static final String TAG = "TChores.RTC Alarm";

    // This value is defined and consumed by app code, so any value will work.
    // There's no significance to this sample using 0.
    public static final int REQUEST_CODE = 0;
    public static final String CHANNEL_ID = "Idunnochannel";
    public static final String CHANNEL_LOCAL_NAME = "The madeup Channel";
    public static final String CHANNEL_DESCRIPTION = "a description of this channel";

    public static void setAlarm(Context context, Intent intent, Intent actionIntent, String sAlarmLocalDateTime) {

        createNotificationChannel(context, CHANNEL_ID, CHANNEL_LOCAL_NAME, CHANNEL_DESCRIPTION);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK /*| Intent.FLAG_ACTIVITY_CLEAR_TASK*/);
        PendingIntent pendingAfterTapNotificationIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK /*| Intent.FLAG_ACTIVITY_CLEAR_TASK*/);
        PendingIntent pendingActionIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // BEGIN_INCLUDE (pending_intent_for_alarm)
        // Because the intent must be fired by a system service from outside the application,
        // it's necessary to wrap it in a PendingIntent.  Providing a different process with
        // a PendingIntent gives that other process permission to fire the intent that this
        // application has created.
        // Also, this code creates a PendingIntent to start an Activity.  To create a
        // BroadcastIntent instead, simply call getBroadcast instead of getIntent.

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION,
                getNotification(context, "THIS IS THE CHORE TEXT TO FILL IN", pendingAfterTapNotificationIntent, pendingActionIntent));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        // Calculate alarm time in Epoch milliseconds
        int alarmType = AlarmManager.RTC_WAKEUP;
        long rtcAlarmMillis = ZonedDateTime.of(LocalDateTime.parse(sAlarmLocalDateTime),ZoneId.systemDefault()).toEpochSecond()*1000;

        Log.i(TAG,"rtcAlarmMillis     = " + String.valueOf(rtcAlarmMillis));
        Log.i(TAG,"currentTimeMillis  = " + String.valueOf(System.currentTimeMillis()));
        Log.i(TAG,"LocalDateTime now  = " + LocalDateTime.now().toString());

        if (rtcAlarmMillis < System.currentTimeMillis())
            return;     // don't set alarms to the past

        // The AlarmManager, like most system services, isn't created by application code, but
        // requested from the system.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(alarmType,rtcAlarmMillis, pendingIntent);

        // END_INCLUDE (configure_alarm_manager);

        Log.i(TAG,"Alarm set.");
    }


    private static Notification getNotification(Context context, String content, PendingIntent pendingIntent, PendingIntent doneChorePendingIntent) {

        //Action Button
        Notification.Action actionCompleted = new Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.fui_ic_check_circle_black_128dp),
                "COMPLETED",
                doneChorePendingIntent).build();


        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID);
        builder.setContentTitle("TChores -- Content TItle in Notification.Builder");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_monetization_on_white_24px);
        builder.setContentIntent(pendingIntent);
        builder.addAction(actionCompleted);
        builder.setAutoCancel(true);
        builder.setCategory(Notification.CATEGORY_ALARM);  // TODO this might change depending on chore
        return builder.build();
    }

    private static void createNotificationChannel(Context context, String channelId, String channelName, String channelDescription) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;  // TODO customize this?  Or is it fine to leave for the user
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
