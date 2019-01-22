package me.tseng.studios.tchores.java;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;

import me.tseng.studios.tchores.java.model.Restaurant;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;


public class NotificationChoreCompleteBR extends BroadcastReceiver {

    private static final String TAG = "NotificationChoreCompleteBR";

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    private FirebaseFirestore mFirestore;


    @Override
    public void onReceive(final Context context, Intent intent) {

        String restaurantId = intent.getExtras().getString(RestaurantDetailActivity.KEY_RESTAURANT_ID);
        if (restaurantId == null) {
            throw new IllegalArgumentException("Must pass extra " + RestaurantDetailActivity.KEY_RESTAURANT_ID);
        }
        String actionId = intent.getExtras().getString(RestaurantDetailActivity.KEY_ACTION);
        if (actionId== null) {
            throw new IllegalArgumentException("Must pass extra " + RestaurantDetailActivity.KEY_ACTION);
        }

        Log.i(TAG, "got into Compelete Broadcast Receiver. restaurantId= " + restaurantId + "  and action id= " + actionId);

        // assume fireauth user is logged in  TODO check fireauth user is logged in
        mFirestore = FirebaseFirestore.getInstance();

        // TODO mark chore complete


        // Reset chore target time
        final DocumentReference drChore = mFirestore.collection("restaurants").document(restaurantId);
        drChore.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            Restaurant chore = document.toObject(Restaurant.class);

                            String id = document.getId();
                            String name = chore.getName();
                            String priorityChannel = chore.getPriorityChannel();
                            Restaurant.RecuranceInterval ri = chore.getRecuranceIntervalAsEnum();

                            LocalDateTime ldt;
                            try {
                                ldt = LocalDateTime.parse(chore.getBDTime());
                            } catch (Exception e) {
                                Log.e(TAG, "Date stored on Firebase database is badly formated.");
                                ldt = LocalDateTime.MIN;
                            }

                            Log.d(TAG, "Got Restaurant: " + id +
                                    " = " + name +
                                    " at " + ldt.toString());

                            if (ldt.isAfter(LocalDateTime.now())) {
                                // This was already bumped
                                return;
                            }

                            // calculate new alarm time
                            ldt = ldt.plusMinutes(2);   // TODO iumplement switch(chore.getPriorityChannel())

                            // record new alarm times for chore into Firestore
                            chore.setBDTime(ldt.toString());
                            chore.setADTime(ldt.toString());
                            drChore.update(Restaurant.FIELD_BDTIME, ldt.toString());
                            drChore.update(Restaurant.FIELD_ADTIME, ldt.toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i(TAG, "ADTime sucessfully updated in Firestore");
                                        }
                                    });

                            // set the alarm
                            AlarmManagerUtil.setAlarm(context, id, ldt.toString(), name, priorityChannel);

                        } else {
                            Log.d(TAG, "Chore get() failed with ", task.getException());
                        }

                    }
                });

        // cancel the notification
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(restaurantId.hashCode());

    }

}


