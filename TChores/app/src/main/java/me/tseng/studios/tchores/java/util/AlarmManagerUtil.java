package me.tseng.studios.tchores.java.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.NotificationChoreCompleteBR;
import me.tseng.studios.tchores.java.AfterAlarmBR;
import me.tseng.studios.tchores.java.RestaurantDetailActivity;
import me.tseng.studios.tchores.java.model.Restaurant;

import static me.tseng.studios.tchores.java.model.Restaurant.RESTAURANT_URI_PREFIX;

public class AlarmManagerUtil {
    private static final String TAG = "TChores.RTC Alarm";

    // This value is defined and consumed by app code, so any value will work.
    // There's no significance to this sample using 0.
    public static final int REQUEST_CODE = 0;


    public static void setAlarm(Context context, String id, String sAlarmLocalDateTime, String sContentTitle, String sPhoto, String notificationChannelId) {

        createNotificationChannel(context);

        Intent intent = buildIntent(context, RestaurantDetailActivity.class, id, RestaurantDetailActivity.ACTION_VIEW);
        PendingIntent pendingAfterTapNotificationIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent action1Intent = buildIntent(context, NotificationChoreCompleteBR.class, id, RestaurantDetailActivity.ACTION_COMPLETED);
        PendingIntent pendingActionIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action action1 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.fui_ic_check_circle_black_128dp),
                RestaurantDetailActivity.ACTION_COMPLETED_LOCALIZED, pendingActionIntent).build();

        Intent action2Intent = buildIntent(context, NotificationChoreCompleteBR.class, id, RestaurantDetailActivity.ACTION_REFUSED);
        PendingIntent pendingAction2Intent = PendingIntent.getBroadcast(context, REQUEST_CODE, action2Intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action action2 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.fui_ic_check_circle_black_128dp),
                RestaurantDetailActivity.ACTION_REFUSED_LOCALIZED, pendingAction2Intent).build();

        Intent action3Intent = buildIntent(context, NotificationChoreCompleteBR.class, id, RestaurantDetailActivity.ACTION_SNOOZED);
        PendingIntent pendingAction3Intent = PendingIntent.getBroadcast(context, REQUEST_CODE, action3Intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action action3 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.fui_ic_check_circle_black_128dp),
                RestaurantDetailActivity.ACTION_SNOOZED_LOCALIZED, pendingAction3Intent).build();


        Intent afterAlarmIntent = buildIntent(context, AfterAlarmBR.class, id, RestaurantDetailActivity.ACTION_VIEW);
        afterAlarmIntent.putExtra(AfterAlarmBR.NOTIFICATION,
                getNotification(context, sContentTitle, "THIS IS THE CHORE TEXT TO FILL IN", sPhoto, notificationChannelId, pendingAfterTapNotificationIntent, action1, action2, action3));
        PendingIntent afterAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, afterAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);


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
        alarmManager.setExactAndAllowWhileIdle(alarmType,rtcAlarmMillis, afterAlarmPendingIntent);

        // END_INCLUDE (configure_alarm_manager);

        Log.i(TAG,"Alarm set. @" + rtcAlarmMillis + " Title: " + sContentTitle + " : " + id);
    }

    @NonNull
    private static Intent buildIntent(Context context, Class classReceiving, String id, String action) {
        Intent intent = new Intent(context, classReceiving);
        intent.setData(Uri.parse(RESTAURANT_URI_PREFIX + id));  // faked just to differentiate alarms on different restaurants
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, id);
        intent.putExtra(RestaurantDetailActivity.KEY_ACTION, action);
        intent.setAction(action); // Needed to differentiate Intents so Notification manager doesn't squash them together
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    private static Notification getNotification(Context context, String sContentTitle, String sContentText, String sPhoto, String channelId, PendingIntent pendingAfterTapNotificationIntent, Notification.Action action1, Notification.Action action2, Notification.Action action3) {

        int resourceSPhoto = -1;
        try {
             resourceSPhoto = Integer.valueOf(sPhoto);
        } catch (Exception e){
            // not an int or not a resource number; use default image
            Log.e(TAG, "There was an illegal resourceId for a chore. sPhoto =" + sPhoto);
        }

        Notification.Builder builder = new Notification.Builder(context, channelId);
        builder.setContentTitle(sContentTitle);
        //builder.setContentText(sContentText);
        if (resourceSPhoto != -1)
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), resourceSPhoto));  // setLargeIcon() before setSmallIcon()
        builder.setSmallIcon(R.drawable.ic_monetization_on_white_24px);
        builder.setContentIntent(pendingAfterTapNotificationIntent);
        builder.addAction(action1);
        builder.addAction(action2);
        builder.addAction(action3);
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
