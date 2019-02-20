package me.tseng.studios.tchores.java.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.ChoreDetailActivity;
import me.tseng.studios.tchores.java.NotificationChoreCompleteBR;
import me.tseng.studios.tchores.java.AfterAlarmBR;
import me.tseng.studios.tchores.java.model.Chore;

import static me.tseng.studios.tchores.java.model.Chore.CHORE_URI_PREFIX;

public class AlarmManagerUtil {
    private static final String TAG = "TChores.RTC Alarm";

    // This value is defined and consumed by app code, so any value will work.
    // There's no significance to this sample using 0.
    public static final int REQUEST_CODE = 0;


    public static void setAlarm(Context context, String id, String sAlarmLocalDateTime, String sContentTitle, String sPhoto, String notificationChannelId, String sScheduledLocalDateTime) {

        createNotificationChannel(context);

        LocalDateTime localDateTimeAlarm = localDateTimeFromString(sAlarmLocalDateTime);

        Intent intent = buildIntent(context, ChoreDetailActivity.class, id, ChoreDetailActivity.ACTION_VIEW);
        PendingIntent pendingAfterTapNotificationIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent action1Intent = buildIntent(context, NotificationChoreCompleteBR.class, id, ChoreDetailActivity.ACTION_COMPLETED);
        PendingIntent pendingActionIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action action1 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.fui_ic_check_circle_black_128dp),
                ChoreDetailActivity.ACTION_COMPLETED_LOCALIZED, pendingActionIntent).build();

        Intent action2Intent = buildIntent(context, NotificationChoreCompleteBR.class, id, ChoreDetailActivity.ACTION_REFUSED);
        PendingIntent pendingAction2Intent = PendingIntent.getBroadcast(context, REQUEST_CODE, action2Intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action action2 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.fui_ic_check_circle_black_128dp),
                ChoreDetailActivity.ACTION_REFUSED_LOCALIZED, pendingAction2Intent).build();

        Intent action3Intent = buildIntent(context, NotificationChoreCompleteBR.class, id, ChoreDetailActivity.ACTION_SNOOZED);
        PendingIntent pendingAction3Intent = PendingIntent.getBroadcast(context, REQUEST_CODE, action3Intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action action3 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.fui_ic_check_circle_black_128dp),
                ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED, pendingAction3Intent).build();


        Intent afterAlarmIntent = buildIntent(context, AfterAlarmBR.class, id, ChoreDetailActivity.ACTION_VIEW);
        afterAlarmIntent.putExtra(AfterAlarmBR.KEY_NOTIFICATION,
                getNotification(context, sContentTitle, "THIS IS THE CHORE TEXT TO FILL IN", sPhoto, notificationChannelId, pendingAfterTapNotificationIntent, action1, action2, action3));
        afterAlarmIntent.putExtra(AfterAlarmBR.KEY_PRIORITY_CHANNEL, notificationChannelId);
        afterAlarmIntent.putExtra(AfterAlarmBR.KEY_CHORE_BDTIME, sScheduledLocalDateTime);

        PendingIntent afterAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, afterAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        setAlarmWIntent(context, localDateTimeAlarm, afterAlarmPendingIntent, true);
    }


    public static void setAlarmWIntent(Context context, LocalDateTime localDateTimeAlarm, PendingIntent afterAlarmPendingIntent, boolean isExactTime) {
        long rtcAlarmMillis = ZonedDateTime.of(localDateTimeAlarm, ZoneId.systemDefault()).toEpochSecond()*1000;     // Calculate alarm time in Epoch milliseconds

//        Log.i(TAG,"rtcAlarmMillis     = " + String.valueOf(rtcAlarmMillis));
//        Log.i(TAG,"currentTimeMillis  = " + String.valueOf(System.currentTimeMillis()));
//        Log.i(TAG,"LocalDateTime now  = " + LocalDateTime.now().toString());

        // The AlarmManager, like most system services, isn't created by application code, but
        // requested from the system.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.cancel(afterAlarmPendingIntent);   // Cancel the backup alarm even before checking if the next alarm will be in the past

        if (rtcAlarmMillis < System.currentTimeMillis()) {
            Log.i(TAG, "Alarm not set because it is in past. @" + localDateTimeAlarm.toString());
            return;     // don't set alarms to the past
        }

        if (isExactTime)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, rtcAlarmMillis, afterAlarmPendingIntent);
        else
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, rtcAlarmMillis, afterAlarmPendingIntent);
        // END_INCLUDE (configure_alarm_manager);

        Log.i(TAG,"Alarm set. @" + localDateTimeAlarm.toString());

        // Note: to see the current alarms on the phone, use  C:\Users\Larry\AppData\Local\Android\Sdk\platform-tools\adb shell dumpsys alarm >dump.txt
    }

    public static LocalDateTime localDateTimeFromString(String sAlarmLocalDateTime) {
        try {
            LocalDateTime localDateTimeAlarm = LocalDateTime.parse(sAlarmLocalDateTime);
            return localDateTimeAlarm;
        } catch (Exception e) {
            Log.e(TAG, "Date in sAlarmLocalDateTime is badly formatted. = " + sAlarmLocalDateTime);
            return LocalDateTime.MIN;
        }
    }

    @NonNull
    private static Intent buildIntent(Context context, Class classReceiving, String id, String action) {
        Intent intent = new Intent(context, classReceiving);
        intent.setData(Uri.parse(CHORE_URI_PREFIX + id));  // faked just to differentiate alarms on different chores
        intent.putExtra(ChoreDetailActivity.KEY_CHORE_ID, id);
        intent.putExtra(ChoreDetailActivity.KEY_ACTION, action);
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

            for (Chore.PriorityChannel pc : Chore.PriorityChannel.values()) {
                NotificationChannel channel = new NotificationChannel(
                        pc.name(),
                        pc.name(),
                        Chore.PriorityChannelImportance(pc));
                channel.setDescription(Chore.PriorityChannelDescription(pc));

                switch (pc) {
                    case CRITICAL:
                        channel.setBypassDnd(true);
                        channel.enableLights(true);
                        channel.setLightColor(Color.WHITE);
                        channel.setVibrationPattern(new long[] {0, 100, 1000, 300, 200, 100, 500, 200, 100});
                        break;
                    case IMPORTANT2SELF:
                        channel.setBypassDnd(false);
                        channel.enableLights(true);
                        channel.setLightColor(Color.BLUE);
                        channel.setVibrationPattern(new long[] {0, 1000, 1000, 300});
                        break;
                    case IMPORTANT2OTHERS:
                        channel.setBypassDnd(false);
                        channel.enableLights(true);
                        channel.setLightColor(Color.RED);
                        channel.setVibrationPattern(new long[] {0, 700, 500, 700});
                        break;
                    case NORMAL:
                    default:
                        channel.setBypassDnd(false);
                        channel.enableLights(false);
                        channel.setLightColor(Color.RED);
                        channel.setVibrationPattern(new long[] {0, 400});
                        break;
                }
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
