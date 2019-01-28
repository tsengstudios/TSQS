package me.tseng.studios.tchores.java;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.time.LocalDateTime;

import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.model.Flurr;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;


public class NotificationChoreCompleteBR extends BroadcastReceiver {

    private static final String TAG = "TChores.NotificationChoreCompleteBR";

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    private FirebaseFirestore mFirestore;


    @Override
    public void onReceive(final Context context, Intent intent) {

        final String choreId = intent.getExtras().getString(ChoreDetailActivity.KEY_CHORE_ID);
        if (choreId == null) {
            throw new IllegalArgumentException("Must pass extra " + ChoreDetailActivity.KEY_CHORE_ID);
        }
        String actionId = intent.getExtras().getString(ChoreDetailActivity.KEY_ACTION);
        if (actionId== null) {
            throw new IllegalArgumentException("Must pass extra " + ChoreDetailActivity.KEY_ACTION);
        }
        String recordedActionLocal = "error: improper action sent";
        Boolean tempSetNormalRecurance = true;
        switch (actionId) {
            case ChoreDetailActivity.ACTION_COMPLETED :
                recordedActionLocal = ChoreDetailActivity.ACTION_COMPLETED_LOCALIZED;

                break;
            case ChoreDetailActivity.ACTION_REFUSED :
                recordedActionLocal = ChoreDetailActivity.ACTION_REFUSED_LOCALIZED;

                break;
            case ChoreDetailActivity.ACTION_SNOOZED :
                recordedActionLocal = ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED;
                tempSetNormalRecurance = false;
                break;
            case ChoreDetailActivity.ACTION_VIEW :
                throw new UnsupportedOperationException("Didn't implement the View action yet");  // TODO maybe useful to have this BR support recasting the View ChoreDetailActivity intent.
                // return; break;
            default:
        }
        final Boolean setNormalRecurance = tempSetNormalRecurance;

        Log.d(TAG, "got into Compelete Broadcast Receiver. choreId= " + choreId + "  and action id= " + actionId);

        // assume fireauth user is logged in  TODO check fireauth user is logged in
        mFirestore = FirebaseFirestore.getInstance();
        final DocumentReference choreRef = mFirestore.collection("chores").document(choreId);

        // mark chore action
        final Flurr flurr = new Flurr(
                FirebaseAuth.getInstance().getCurrentUser(),
                1,
                recordedActionLocal,
                choreId);
        final DocumentReference flurrRef = mFirestore.collection("flurrs").document();  // Create reference for new flurr, for use inside the transaction

//        // Update the flurr timestamp field with the value from the server
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("timestamp", FieldValue.serverTimestamp());
//        ratingRef.update(updates);

        // In a transaction, add the new flurr and update the aggregate totals and Reset chore target time
        mFirestore.runTransaction(new Transaction.Function<Chore>() {
            @Override
            public Chore apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot choreSnapshot = transaction.get(choreRef);
                Chore chore = choreSnapshot.toObject(Chore.class);

                        // Compute new number of ratings
                        int newNumRatings = chore.getNumRatings() + 1;

                        // Compute new average flurr
                        double oldRatingTotal = chore.getAvgRating() * chore.getNumRatings();
                        double newAvgRating = (oldRatingTotal + flurr.getFlurr()) / newNumRatings;

                        // Set new chore info
                        chore.setNumRatings(newNumRatings);
                        chore.setAvgRating(newAvgRating);

                String name = chore.getName();
                Chore.RecuranceInterval ri = chore.getRecuranceIntervalAsEnum();

                LocalDateTime ldt;
                try {
                    ldt = LocalDateTime.parse(chore.getADTime());
                } catch (Exception e) {
                    Log.e(TAG, "Date stored on Firebase database is badly formated.");
                    ldt = LocalDateTime.MIN;
                }

                Log.d(TAG, "Got Chore: " + choreId +
                        " -  Alarm was at " + ldt.toString() +
                        " Name=" + name);

                if (ldt.isAfter(LocalDateTime.now())) {
                    // This was already bumped
                    throw new FirebaseFirestoreException("Weird -- this action is trying to bump the alarm time when it is already in the future.",
                            FirebaseFirestoreException.Code.INVALID_ARGUMENT);
                }

                // calculate new alarm time
                // record new alarm times for chore into Firestore
                if (setNormalRecurance) {
                    switch (chore.getRecuranceIntervalAsEnum()) {
                        case HOURLY:
                            ldt = ldt.plusMinutes(60);
                            break;
                        case DAILY:
                            ldt = ldt.plusDays(1);
                            break;
                        case WEEKLY:
                            ldt = ldt.plusWeeks(1);
                            break;
                        default:
                            throw new UnsupportedOperationException("not finished building recurance interval support");
                    }

                    // TODO iumplement switch(chore.getPriorityChannel())  perhaps we shouldn't be allowing snooze for 2 hours, and we need to act on this snooze action....

                    chore.setBDTime(ldt.toString());
                    choreRef.update(Chore.FIELD_BDTIME, ldt.toString());

                } else {
                    ldt = LocalDateTime.now().plusMinutes(2);   // TODO proper snooze of 10 minutes later...
                    // DO NOT set or update BDTime on !setNormalRecurance / snooze action
                    // chore.setBDTime(ldt.toString());
                    // choreRef.update(Chore.FIELD_BDTIME, ldt.toString());
                }

                chore.setADTime(ldt.toString());

                // Commit to Firestore
                transaction.set(choreRef, chore);
                transaction.set(flurrRef, flurr);

                return chore;
            }

        }).addOnSuccessListener(new OnSuccessListener<Chore>() {
            @Override
            public void onSuccess(Chore chore) {
            Log.i(TAG, "Chore action now marked");

            // TODO toast success


            // cancel the notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(choreId.hashCode());

            // TODO cancel the chat head

            // set the new alarm
            AlarmManagerUtil.setAlarm(context, choreId, chore.getADTime(), chore.getName(), chore.getPhoto(), chore.getPriorityChannel());

            // TODO compute awards here?

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Chore action marking failed", e);
            }
        });

    }

}


