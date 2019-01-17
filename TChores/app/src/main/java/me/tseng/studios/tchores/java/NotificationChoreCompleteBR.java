package me.tseng.studios.tchores.java;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationChoreCompleteBR extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {

        String restaurantId = intent.getExtras().getString(RestaurantDetailActivity.KEY_RESTAURANT_ID);
        if (restaurantId == null) {
            throw new IllegalArgumentException("Must pass extra " + RestaurantDetailActivity.KEY_RESTAURANT_ID);
        }
        String actionId = intent.getExtras().getString(RestaurantDetailActivity.KEY_ACTION);
        if (actionId== null) {
            throw new IllegalArgumentException("Must pass extra " + RestaurantDetailActivity.KEY_ACTION);
        }

        Log.i("NotificationChoreCompleteBR", "got into Compelete Broadcast Receiver. restaurantId= " + restaurantId + "  and action id= " + actionId);

    }

}


