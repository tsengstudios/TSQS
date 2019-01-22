package me.tseng.studios.tchores.java;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.time.LocalDateTime;

import me.tseng.studios.tchores.java.model.Rating;
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
        final DocumentReference drChore = mFirestore.collection("restaurants").document(restaurantId);

        // mark chore complete
        Rating rating = new Rating(
                FirebaseAuth.getInstance().getCurrentUser(),
                1,
                actionId);

        // In a transaction, add the new rating and update the aggregate totals
        addRatingComplete(drChore, rating)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Chore marked completed now");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Chore marking failed", e);
                    }
                });

        // TODO make Completion one transaction instead of currently adding a rating (above), and then modifying the new aDTime and bDTime. (below)

        // Reset chore target time
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

    private Task<Void> addRatingComplete(final DocumentReference restaurantRef, final Rating rating) {
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = restaurantRef.collection("ratings").document();

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                Restaurant restaurant = transaction.get(restaurantRef).toObject(Restaurant.class);

                // Compute new number of ratings
                int newNumRatings = restaurant.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = restaurant.getAvgRating() * restaurant.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) / newNumRatings;

                // Set new restaurant info
                restaurant.setNumRatings(newNumRatings);
                restaurant.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(restaurantRef, restaurant);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }


}


