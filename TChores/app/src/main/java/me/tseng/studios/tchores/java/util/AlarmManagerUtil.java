package me.tseng.studios.tchores.java.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.NotificationChoreCompleteBR;
import me.tseng.studios.tchores.java.NotificationPublisher;
import me.tseng.studios.tchores.java.RestaurantAddActivity;
import me.tseng.studios.tchores.java.RestaurantDetailActivity;
import me.tseng.studios.tchores.java.model.Restaurant;

import static me.tseng.studios.tchores.java.model.Restaurant.RESTAURANT_URI_PREFIX;

public class AlarmManagerUtil {
    private static final String TAG = "TChores.RTC Alarm";

    // This value is defined and consumed by app code, so any value will work.
    // There's no significance to this sample using 0.
    public static final int REQUEST_CODE = 0;


    public static void setAlarm(Context context, String id, String sAlarmLocalDateTime, String sContentTitle, String notificationChannelId) {

        createNotificationChannel(context);

        Intent intent = new Intent(context, RestaurantDetailActivity.class);
        intent.setData(Uri.parse(RESTAURANT_URI_PREFIX + id));  // faked just to differentiate alarms on different restaurants
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, id);
        intent.putExtra(RestaurantDetailActivity.KEY_ACTION, RestaurantDetailActivity.ACTION_VIEW);
        intent.setAction(RestaurantDetailActivity.ACTION_VIEW); // Needed to differentiate Intents so Notification manager doesn't squash them together
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK /*| Intent.FLAG_ACTIVITY_CLEAR_TASK*/);
        PendingIntent pendingAfterTapNotificationIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent actionIntent = new Intent(context, NotificationChoreCompleteBR.class);
        actionIntent.setData(Uri.parse(RESTAURANT_URI_PREFIX + id));  // faked just to differentiate alarms on different restaurants
        actionIntent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, id);
        actionIntent.putExtra(RestaurantDetailActivity.KEY_ACTION, RestaurantDetailActivity.ACTION_COMPLETED);
        actionIntent.setAction(RestaurantDetailActivity.ACTION_COMPLETED);
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
        notificationIntent.setData(Uri.parse(RESTAURANT_URI_PREFIX + id));  // faked just to differentiate alarms on different restaurants
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION,
                getNotification(context, sContentTitle,"THIS IS THE CHORE TEXT TO FILL IN", notificationChannelId, pendingAfterTapNotificationIntent, pendingActionIntent));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        // Calculate alarm time in Epoch milliseconds
        int alarmType = AlarmManager.RTC_WAKEUP;
        long rtcAlarmMillis;
        try {
            rtcAlarmMillis = ZonedDateTime.of(LocalDateTime.parse(sAlarmLocalDateTime),ZoneId.systemDefault()).toEpochSecond()*1000;
        } catch (Exception e) {
            Log.e(TAG, "Date in sAlarmLocalDateTime is badly formatted. = " + sAlarmLocalDateTime);
            rtcAlarmMillis = 0;
        }

//        Log.i(TAG,"rtcAlarmMillis     = " + String.valueOf(rtcAlarmMillis));
//        Log.i(TAG,"currentTimeMillis  = " + String.valueOf(System.currentTimeMillis()));
//        Log.i(TAG,"LocalDateTime now  = " + LocalDateTime.now().toString());

        if (rtcAlarmMillis < System.currentTimeMillis())
            return;     // don't set alarms to the past

        // The AlarmManager, like most system services, isn't created by application code, but
        // requested from the system.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(alarmType,rtcAlarmMillis, pendingIntent);

        // END_INCLUDE (configure_alarm_manager);

        Log.i(TAG,"Alarm set. @" + rtcAlarmMillis + " Title: " + sContentTitle + " : " + id);
    }


    private static Notification getNotification(Context context, String sContentTitle, String sContentText, String channelId, PendingIntent pendingIntent, PendingIntent doneChorePendingIntent) {

        //Action Button
        Notification.Action actionCompleted = new Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.fui_ic_check_circle_black_128dp),
                "COMPLETED",
                doneChorePendingIntent).build();


        Notification.Builder builder = new Notification.Builder(context, channelId);
        builder.setContentTitle(sContentTitle);
        //builder.setContentText(sContentText);
        builder.setSmallIcon(R.drawable.ic_monetization_on_white_24px);
        builder.setContentIntent(pendingIntent);
        builder.addAction(actionCompleted);
        builder.setAutoCancel(false);
        builder.setCategory(Notification.CATEGORY_ALARM);  // TODO this might change depending on chore
        return builder.build();
    }

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // TODO Perhaps we should only do this if NotificationManager.getNotificationChannels().count() == 0

            for (Restaurant.PriorityChannel pc : Restaurant.PriorityChannel.values()) {
                NotificationChannel channel = new NotificationChannel(
                        pc.name(),
                        pc.name(),
                        Restaurant.PriorityChannelImportance(pc));
                channel.setDescription(Restaurant.PriorityChannelDescription(pc));

                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
