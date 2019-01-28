package me.tseng.studios.tchores.java;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import static me.tseng.studios.tchores.java.model.Chore.chore_URI_PREFIX;


public class AfterAlarmBR extends BroadcastReceiver {

    private static final String TAG = "TChores.AfterAlarmBR";

    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        String id = intent.getStringExtra(ChoreDetailActivity.KEY_CHORE_ID);
        notificationManager.notify(id.hashCode(), notification);    // hashCode won't guarantee uniqueness, but probably for two alarms at the same time?
        Log.i(TAG, "Fire notification id = " + id.hashCode());

        Intent startHoverIntent = new Intent(context, TChoreHoverMenuService.class);
        startHoverIntent.setData(Uri.parse(chore_URI_PREFIX + id));  // faked just to differentiate alarms on different chores
        startHoverIntent.putExtra(ChoreDetailActivity.KEY_CHORE_ID, id);
        startHoverIntent.putExtra(NOTIFICATION, notification);
        context.startService(startHoverIntent);
    }

}


