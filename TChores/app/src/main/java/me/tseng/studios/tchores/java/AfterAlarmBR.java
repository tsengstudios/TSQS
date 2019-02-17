package me.tseng.studios.tchores.java;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.time.LocalDateTime;

import me.tseng.studios.tchores.BuildConfig;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;

import static me.tseng.studios.tchores.java.model.Chore.CHORE_URI_PREFIX;


public class AfterAlarmBR extends BroadcastReceiver {

    private static final String TAG = "TChores.AfterAlarmBR";

    public static long MINUTES_BACKUP_ALARM = 2;
    public static long MAXMINUTESCRITICAL = 4;

    public static String KEY_NOTIFICATION = BuildConfig.APPLICATION_ID + ".notification";
    public static String KEY_ENSURE_PRIORITY = BuildConfig.APPLICATION_ID + ".ensure_priority";
    public static String KEY_PRIORITY_CHANNEL = BuildConfig.APPLICATION_ID + ".priority_channel";
    public static String KEY_CHORE_BDTIME = BuildConfig.APPLICATION_ID + ".chore_bdtime";


    @Override
    public void onReceive(Context context, Intent intent) {

        boolean ensurePriority = intent.getBooleanExtra(KEY_ENSURE_PRIORITY, false);
        String sPriorityChannel = intent.getStringExtra(KEY_PRIORITY_CHANNEL);
        Chore.PriorityChannel priorityChannel = Chore.getPriorityChannelFromString(sPriorityChannel);
        String sChoreBdTime = intent.getStringExtra(KEY_CHORE_BDTIME);
        LocalDateTime ldtChoreBDTime = AlarmManagerUtil.localDateTimeFromString(sChoreBdTime);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(KEY_NOTIFICATION);
        String id = intent.getStringExtra(ChoreDetailActivity.KEY_CHORE_ID);

        Intent startHoverIntent = new Intent(context, TChoreHoverMenuService.class);
        startHoverIntent.setData(Uri.parse(CHORE_URI_PREFIX + id));  // faked just to differentiate alarms on different chores
        startHoverIntent.putExtra(ChoreDetailActivity.KEY_CHORE_ID, id);
        startHoverIntent.putExtra(KEY_NOTIFICATION, notification);

        Log.i(TAG, sPriorityChannel + " Alarm received for id = " + id.hashCode());

        if (!ensurePriority) {
            notificationManager.notify(id.hashCode(), notification);    // hashCode won't guarantee uniqueness, but probably for two alarms at the same time?

            context.startService(startHoverIntent);
        } else {
            // Assume
            switch (priorityChannel) {
                case NORMAL:
                    // let it go?  Should we not even have set a backup alarm
                    return;

                case CRITICAL:
                    if (ldtChoreBDTime.isBefore(LocalDateTime.now().minusMinutes(MAXMINUTESCRITICAL))) {
                        // fire notification to others?
                        Log.i(TAG, "Critical alarm time has past.");

                    }
                    // fire notification and hover again

                    break;
                case IMPORTANT2SELF:
                    break;
                case IMPORTANT2OTHERS:
                    break;

                default:
                    break;
            }

            // reshow notification and hover
            notificationManager.notify(id.hashCode(), notification);    // hashCode won't guarantee uniqueness, but probably for two alarms at the same time?

            context.startService(startHoverIntent);

        }

        // set Backup Alarm

        intent.putExtra(KEY_ENSURE_PRIORITY, true);
        PendingIntent afterAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        LocalDateTime ldt = LocalDateTime.now().plusMinutes(MINUTES_BACKUP_ALARM);
        AlarmManagerUtil.setAlarmWIntent(context, ldt, afterAlarmPendingIntent, false);
    }

}


