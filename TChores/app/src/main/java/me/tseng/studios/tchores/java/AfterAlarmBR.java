package me.tseng.studios.tchores.java;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.time.LocalDateTime;

import me.tseng.studios.tchores.BuildConfig;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;

import static me.tseng.studios.tchores.java.model.Chore.CHORE_URI_PREFIX;


public class AfterAlarmBR extends BroadcastReceiver {

    private static final String TAG = "TChores.AfterAlarmBR";

    public static String KEY_NOTIFICATION = BuildConfig.APPLICATION_ID + ".notification";
    public static String KEY_ENSURE_PRIORITY = BuildConfig.APPLICATION_ID + ".ensure_priority";
    public static String KEY_PRIORITY_CHANNEL = BuildConfig.APPLICATION_ID + ".priority_channel";
    public static String KEY_CHORE_BDTIME = BuildConfig.APPLICATION_ID + ".chore_bdtime";
    public static String KEY_CHORE_BACKUPNOTIFICATIONDELAY = BuildConfig.APPLICATION_ID + ".chore_backupnotificationdelay";
    public static String KEY_CHORE_CRITICALBACKUPTIME = BuildConfig.APPLICATION_ID + ".chore_criticalbackuptime";

    public static String CRITICAL_NOTIFY_PHONENUMBERS = "+14259859263,+14253123969";


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

        notificationManager.notify(id.hashCode(), notification);    // hashCode won't guarantee uniqueness, but probably for two alarms at the same time?

        context.startService(startHoverIntent);

        switch (priorityChannel) {
            case NORMAL:
                // let it go.  do not set a backup alarm
                break;

            case CRITICAL:
            case IMPORTANT2OTHERS:
                int maxMinutes = intent.getIntExtra(KEY_CHORE_CRITICALBACKUPTIME, Integer.MAX_VALUE);
                if (!LocalDateTime.now().isBefore(ldtChoreBDTime.plusMinutes(maxMinutes))) {
                    // fire notification to others?
                    Log.i(TAG, "Critical alarm time has past.");

                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "No permission to send SMS upon a chore\'s Critical Time", Toast.LENGTH_LONG);
                    } else {
                        String sContentTitle = notification.extras.getString(Notification.EXTRA_TITLE);

                        // split by commas, stripping whitespace afterwards
                        String numbers[] = CRITICAL_NOTIFY_PHONENUMBERS.split(", *");
                        String textText = String.format(context.getString(R.string.sms_format_string_message), sContentTitle);

                        SmsManager smsManager = SmsManager.getDefault();
                        for (String number : numbers) {
                            smsManager.sendTextMessage(number, null, textText, null, null);
                        }
                    }
                }

            case IMPORTANT2SELF:
                // set Backup Alarm
                long minutesBackupAlarm = intent.getIntExtra(KEY_CHORE_BACKUPNOTIFICATIONDELAY, Integer.MAX_VALUE);
                LocalDateTime ldt = LocalDateTime.now().plusMinutes(minutesBackupAlarm);
                intent.putExtra(KEY_ENSURE_PRIORITY, true);
                PendingIntent afterAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManagerUtil.setAlarmWIntent(context, ldt, afterAlarmPendingIntent, false);

                break;

            default:
                Log.e(TAG, "Unsupported PriorityChannel");
                break;
        }

    }

}


