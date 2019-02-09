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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.model.Flurr;
import me.tseng.studios.tchores.java.model.Sunshine;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;


public class NotificationChoreCompleteBR extends BroadcastReceiver {

    private static final String TAG = "TChores.NotificationChoreCompleteBR";

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    public class Tuple2<T1,T2> {
        private T1 f1;
        private T2 f2;
        Tuple2(T1 f1, T2 f2) {
            this.f1 = f1; this.f2 = f2;
        }
        public T1 getF1() {return f1;}
        public T2 getF2() {return f2;}
    }

    private FirebaseFirestore mFirestore;


    @Override
    public void onReceive(final Context context, Intent intent) {

        final String choreId = Objects.requireNonNull(intent.getExtras()).getString(ChoreDetailActivity.KEY_CHORE_ID);
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
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String sUserId = firebaseUser.getUid();

        // mark chore action
        final Flurr flurr = new Flurr(
                firebaseUser,
                1,
                recordedActionLocal,
                choreId,
                "TEMP Needs Replacing");
        final DocumentReference flurrRef = mFirestore.collection("flurrs").document();  // Create reference for new flurr, for use inside the transaction

        final DocumentReference choreRef = mFirestore.collection("chores").document(choreId);

//        // Update the flurr timestamp field with the value from the server
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("timestamp", FieldValue.serverTimestamp());
//        ratingRef.update(updates);

        // In a transaction, add the new flurr and update the aggregate totals and Reset chore target time
        mFirestore.runTransaction(new Transaction.Function<Tuple2<Chore,Flurr>>() {
            @Override
            public Tuple2<Chore,Flurr> apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot choreSnapshot = transaction.get(choreRef);
                Chore chore = choreSnapshot.toObject(Chore.class);
                chore.setid(choreId);   // for recordChoreIntoSunshine()

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

                // Before changing BDTime, record the chore BDTime upon which the user just acted
                flurr.setChoreBDTime(chore.getBDTime());

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

                    chore.setADTime(ldt.toString());
                    chore.setBDTime(ldt.toString());
                    // of course this is exactly where we do NOT update Chore.FIELD_DATEUSERLASTSET

                } else {
                    ldt = LocalDateTime.now().plusMinutes(2);   // TODO proper snooze of 10 minutes later...
                    chore.setADTime(ldt.toString());
                    // DO NOT set or update BDTime on !setNormalRecurance / snooze action
                    // chore.setBDTime(ldt.toString());
                }


                // Commit to Firestore
                transaction.set(choreRef, chore);
                transaction.set(flurrRef, flurr);

                return new Tuple2(chore,flurr);
            }

        }).addOnSuccessListener(new OnSuccessListener<Tuple2<Chore,Flurr>>() {
            @Override
            public void onSuccess(Tuple2<Chore,Flurr> tupleCF) {
                Chore chore = tupleCF.getF1();
                Flurr flurr = tupleCF.getF2();

                Log.i(TAG, "Chore action now marked");

                recordChoreIntoSunshine(chore, flurr, sUserId);


                // cancel the notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(choreId.hashCode());

                // TODO cancel the chat head --  OR give feedback it was accomplished first then go away.

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

    private static final int SUNSHINE_LIMIT = 9;       // look back this many Sunshines


    private void recordChoreIntoSunshine(final Chore chore, final Flurr flurr, final String userId) {
        LocalDate sunshineDay = Chore.LocalDateFromString(flurr.getChoreBDTime());
        final String sSunshineDay = sunshineDay.toString();

        // get the right sunshine
        // try to get the current sunshine  (is the docref proof of existance?)
        mFirestore.collection(Sunshine.COLLECTION_PATHNAME)
            .orderBy(Sunshine.FIELD_DAY, Query.Direction.DESCENDING)
            .whereEqualTo(Sunshine.FIELD_USERID, userId)
            .limit(SUNSHINE_LIMIT)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    boolean isProperSunshineFound = false;

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.getString(Sunshine.FIELD_DAY).equals(sSunshineDay)) {
                                isProperSunshineFound = recordChoreIntoPromisingSunshine(isProperSunshineFound, document, flurr);
                                if (isProperSunshineFound)
                                    break;
                            }

                        }
                        if (!isProperSunshineFound) {
                            // never found the right Sunshine.  Create one now
                            Sunshine sunshine = new Sunshine(userId, sSunshineDay);
                            sunshine.initChoreList();
                            sunshine.addChore(chore);
                            flurr.setTimestamp(Timestamp.now());
                            sunshine.addFirstAndOnlyFlurr(flurr);

                            mFirestore.collection(Sunshine.COLLECTION_PATHNAME)
                                .add(sunshine)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.i(TAG, "Success on updating Sunshine");

                                        // TODO  trigger reviewSunshineWithChores(userId, );
                                    };
                                });

                        }
                    } else {
                        throw new RuntimeException("Failed Sunshine query.");
                    }

                }
            });

    }

    /*  Continue the search for the right sunshine, and record the flurr if it is the right sunshine
     *  return true if sunshine already contains the flurr's chore
     */
    private boolean recordChoreIntoPromisingSunshine(boolean isProperSunshineFound, QueryDocumentSnapshot document, Flurr flurr) {

        Sunshine sunshine = document.toObject(Sunshine.class);
        if (isProperSunshineFound) {
            // we already found a proper sunshine this must indicate a duplicate
            // TODO handle consolidating sunshines
            return isProperSunshineFound;
        }

        // add this flurr
        int indexFlurr = sunshine.getChoreIds().indexOf(flurr.getChoreId());
        if (indexFlurr >= 0) {
            DocumentReference sunshineRef = mFirestore.collection(Sunshine.COLLECTION_PATHNAME).document(document.getId());

            if (flurr.getText() == ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED) {
                sunshine.getChoreFlSnoozeCount().set(indexFlurr, 1 + sunshine.getChoreFlSnoozeCount().get(indexFlurr));
                sunshineRef.update(Sunshine.FIELD_CHOREFLSNOOZECOUNT, sunshine.getChoreFlSnoozeCount());
            }

            sunshine.getChoreFlState().set(indexFlurr, flurr.getText());
            sunshineRef.update(Sunshine.FIELD_CHOREFLSTATE, sunshine.getChoreFlState());

            // TODO Check FIELD_AWARDPERFECTDAY
            if (!sunshine.getChoreFlState().contains(ChoreDetailActivity.ACTION_REFUSED_LOCALIZED) &&
                !sunshine.getChoreFlState().contains(ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED)) {
                sunshine.setAwardPerfectDay(true);  // just for completeness
                sunshineRef.update(Sunshine.FIELD_AWARDPERFECTDAY, true);
            }

            sunshine.getChoreFlTimestamp().set(indexFlurr, Timestamp.now());
            sunshineRef.update(Sunshine.FIELD_CHOREFLTIME, sunshine.getChoreFlTimestamp())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Success on updating Sunshine");
                    };
                });

            isProperSunshineFound = true;

        } else {
            Log.e(TAG, "choreId not found means this wasn't precalc-ed properly?");
            // perhaps this means we needed to consolidate Sunshines?

        }
        return isProperSunshineFound;
    }


}


