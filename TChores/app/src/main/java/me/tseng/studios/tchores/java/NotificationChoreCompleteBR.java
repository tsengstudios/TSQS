package me.tseng.studios.tchores.java;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

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
import com.google.type.DayOfWeek;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import me.tseng.studios.tchores.BuildConfig;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.model.Flurr;
import me.tseng.studios.tchores.java.model.Sunshine;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;
import me.tseng.studios.tchores.java.util.ChoreUtil;

import static me.tseng.studios.tchores.java.model.Chore.CHORE_URI_PREFIX;


public class NotificationChoreCompleteBR extends BroadcastReceiver {

    private static final String TAG = "TChores.NotificationChoreCompleteBR";
    public static final String MAINACTIVITY_LOGIN_URI = "login:me";
    public static final String ACTION_LOGIN = BuildConfig.APPLICATION_ID + ".LOGIN";    // Prefix for Intent Action
    private static final int REQUEST_CODE = 1;
    public static final int NOTIFICATION_ID_LOGIN = 88;     // id needed to cancel notification

    private static Context mContext;

    public static class Tuple2<T1,T2> {
        private T1 f1;
        private T2 f2;
        Tuple2(T1 f1, T2 f2) {
            this.f1 = f1; this.f2 = f2;
        }
        public T1 getF1() {return f1;}
        public T2 getF2() {return f2;}
    }



    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "onReceive()");
        mContext = context;

        toast("onReceive()");

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user;
                user = firebaseAuth.getCurrentUser();   // TODO SECURITY ISSUE: we need to null the global user  if they logout....
                if (user != null) {
                    FirebaseAuth.getInstance().removeAuthStateListener(this);
                    //do stuff
                    Log.i(TAG, "onReceive() a chore is complete, login now complete.  FirebaseUser=" + user.getUid());
                    toast("onReceive() a chore is complete, login now complete");
                    postLogin(context, intent, user);

                    // cancel the login notification
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(NOTIFICATION_ID_LOGIN);
                }
                else {
                    // trigger login
                    Log.i(TAG, "onReceive() a chore is complete, but need to login");
                    toast("onReceive() there is no current user.");

                    // collapse chathead
                    Intent startHoverIntent = new Intent(context, TChoreHoverMenuService.class);
                    startHoverIntent.putExtra(ChoreDetailActivity.KEY_CHORE_ID, "PHONY_CHORE_ID");
                    startHoverIntent.putExtra(TChoreHoverMenuService.KEY_COLLAPSE_CHAT_HEAD,true );
                    context.startService(startHoverIntent);

                    // notification to login
                    Intent intentNotifyLogin = new Intent(context, MainActivity.class);
                    intentNotifyLogin.setData(Uri.parse(MAINACTIVITY_LOGIN_URI));
                    intentNotifyLogin.setAction(ACTION_LOGIN); // Needed to differentiate Intents so Notification manager doesn't squash them together
                    intentNotifyLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, intentNotifyLogin, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification.Builder builder = new Notification.Builder(context, Chore.PriorityChannel.CRITICAL.name());
                    builder.setContentTitle("Please Login");
                    builder.setContentText("Need to be logged-in before registering a chore action");
                    builder.setSmallIcon(R.drawable.ic_monetization_on_white_24px);
                    builder.setContentIntent(pendingIntent);
                    builder.setAutoCancel(false);
                    builder.setCategory(Notification.CATEGORY_ALARM);  // TODO this might change depending on chore
                    Notification notification = builder.build();

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID_LOGIN, notification);    // hashCode won't guarantee uniqueness, but probably for two alarms at the same time?
                }
            }
        });

    }

    public static void postLogin(final Context context, Intent intent, FirebaseUser user) {

        final String choreId = Objects.requireNonNull(intent.getExtras()).getString(ChoreDetailActivity.KEY_CHORE_ID);
        if (choreId == null) {
            throw new IllegalArgumentException("Must pass extra " + ChoreDetailActivity.KEY_CHORE_ID);
        }
        String actionId = intent.getExtras().getString(ChoreDetailActivity.KEY_ACTION);
        if (actionId== null) {
            throw new IllegalArgumentException("Must pass extra " + ChoreDetailActivity.KEY_ACTION);
        }

        toast(context, "postLogin() after argument check");

        String recordedActionLocal = "error: improper action sent";
        Boolean tempSetNormalRecurrence = true;
        switch (actionId) {
            case ChoreDetailActivity.ACTION_COMPLETED :
                recordedActionLocal = ChoreDetailActivity.ACTION_COMPLETED_LOCALIZED;

                break;
            case ChoreDetailActivity.ACTION_REFUSED :
                recordedActionLocal = ChoreDetailActivity.ACTION_REFUSED_LOCALIZED;

                break;
            case ChoreDetailActivity.ACTION_SNOOZED :
                recordedActionLocal = ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED;
                tempSetNormalRecurrence = false;
                break;
            case ChoreDetailActivity.ACTION_VIEW :
                throw new UnsupportedOperationException("Didn't implement the View action yet");  // TODO maybe useful to have this BR support recasting the View ChoreDetailActivity intent.
                // return; break;
            default:
        }
        final Boolean setNormalRecurrence = tempSetNormalRecurrence;

        Log.d(TAG, "postLogin() just before getting user again. choreId= " + choreId + "  and action id= " + actionId);

        // assume fireauth user is logged in  TODO check fireauth user is logged in
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final String sUserId = user.getUid();

        // mark chore action
        final Flurr flurr = new Flurr(
                user,
                1,
                recordedActionLocal,
                choreId,
                "TEMP Needs Replacing");
        final DocumentReference flurrRef = firestore.collection("flurrs").document();  // Create reference for new flurr, for use inside the transaction

        final DocumentReference choreRef = firestore.collection("chores").document(choreId);

//        // Update the flurr timestamp field with the value from the server
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("timestamp", FieldValue.serverTimestamp());
//        ratingRef.update(updates);

        Log.i(TAG, "postLogin() just before runTransaction.");

        // In a transaction, add the new flurr and update the aggregate totals and Reset chore target time
        firestore.runTransaction(new Transaction.Function<Tuple2<Chore,Flurr>>() {
            @Override
            public Tuple2<Chore,Flurr> apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot choreSnapshot = transaction.get(choreRef);
                Chore chore = choreSnapshot.toObject(Chore.class);
                chore.setid(choreId);   // for recordChoreIntoSunshine()

                // Before changing BDTime, record the chore BDTime upon which the user just acted
                flurr.setChoreBDTime(chore.getBDTime());

                LocalDateTime ldtA = AlarmManagerUtil.localDateTimeFromString(chore.getADTime());

                Log.d(TAG, "Got Chore: " + choreId +
                        " -  Alarm was at " + ldtA.toString() +
                        " Name=" + chore.getName());

                if (ldtA.isAfter(LocalDateTime.now())) {
                    // This was already bumped
                    throw new FirebaseFirestoreException("Weird -- this action is trying to bump the alarm time when it is already in the future." +
                            " -  Alarm was at " + ldtA.toString(),
                            FirebaseFirestoreException.Code.INVALID_ARGUMENT);
                }

                // calculate new alarm time
                // record new alarm times for chore into Firestore
                if (setNormalRecurrence) {
                    boolean bChangeDTime = true;
                    LocalDateTime ldtB = AlarmManagerUtil.localDateTimeFromString(chore.getBDTime());

                    while (ldtB.isBefore(LocalDateTime.now()) && bChangeDTime) {
                        switch (chore.getRecurrenceIntervalAsEnum()) {
                            case ONLY1OCCURANCE:
                                // do not change ADTime or BDTime
                                bChangeDTime = false;
                                break;
                            case HOURLY:
                                ldtB = ldtB.plusMinutes(60);
                                break;
                            case THREETIMESADAY:
                                ldtB = ldtB.plusHours(8);
                                break;
                            case DAILY:
                                ldtB = ldtB.plusDays(1);
                                break;
                            case WEEKLY:
                                ldtB = ldtB.plusWeeks(1);
                                break;
                            case WEEKDAYS:
                                ldtB = ldtB.plusDays(1);
                                if (ldtB.getDayOfWeek().equals(DayOfWeek.SATURDAY))
                                    ldtB = ldtB.plusDays(2);
                                break;
                            case WEEKENDS:
                                do {
                                    ldtB = ldtB.plusDays(1);
                                }
                                while ((!ldtB.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !ldtB.getDayOfWeek().equals(DayOfWeek.SUNDAY)));
                                break;
                            case BIWEEKLY:
                                ldtB = ldtB.plusWeeks(2);
                                break;
                            case MONTHLY:
                                ldtB = ldtB.plusMonths(1);
                                break;

                            default:
                                throw new UnsupportedOperationException("not finished building recurrence interval support");
                        }
                    }

                    // TODO iumplement switch(chore.getPriorityChannel())  perhaps we shouldn't be allowing snooze for 2 hours, and we need to act on this snooze action....

                    if (bChangeDTime) {
                        chore.setADTime(ldtB.toString());
                        chore.setBDTime(ldtB.toString());
                        // of course this is exactly where we do NOT update Chore.FIELD_DATEUSERLASTSET
                    }

                } else {
                    ldtA = LocalDateTime.now().plusMinutes(chore.getSnoozeMinutes());   // TODO proper snooze of 10 minutes later...
                    chore.setADTime(ldtA.toString());
                    // DO NOT set or update BDTime on !setNormalRecurrence / snooze action
                    // chore.setBDTime(ldtA.toString());
                }


                // Commit to Firestore
                transaction.set(choreRef, chore);
                transaction.set(flurrRef, flurr);

                Log.i(TAG, "end of transaction");
                return new Tuple2(chore,flurr);
            }

        }).addOnSuccessListener(new OnSuccessListener<Tuple2<Chore,Flurr>>() {
            @Override
            public void onSuccess(Tuple2<Chore,Flurr> tupleCF) {
                Chore chore = tupleCF.getF1();
                Flurr flurr = tupleCF.getF2();

                Log.i(TAG, "Chore action now marked");

                // set the new alarm (also cancels the backup alarm)
                AlarmManagerUtil.setAlarm(context, chore);


                recordChoreIntoSunshine(firestore, chore, flurr, sUserId);


                // cancel the notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(choreId.hashCode());

                // Cancel the chat head
                Intent startHoverIntent = new Intent(context, TChoreHoverMenuService.class);
                startHoverIntent.setData(Uri.parse(CHORE_URI_PREFIX + choreId));  // faked just to differentiate alarms on different chores
                startHoverIntent.putExtra(ChoreDetailActivity.KEY_CHORE_ID, choreId);
                startHoverIntent.putExtra(TChoreHoverMenuService.KEY_CHORE_RESOLVED,true );
                context.startService(startHoverIntent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Chore action marking failed", e);
            }
        });

        toast(context,"postLogin() attempt transaction");
    }

    private static final int SUNSHINE_LIMIT = 9;       // look back this many Sunshines


    private static void recordChoreIntoSunshine(final FirebaseFirestore firestore, final Chore chore, final Flurr flurr, final String userId) {
        LocalDate sunshineDay = ChoreUtil.LocalDateFromLocalDateTimeString(flurr.getChoreBDTime());
        final String sSunshineDay = sunshineDay.toString();

        // get the right sunshine
        // try to get the current sunshine  (is the docref proof of existance?)
        firestore.collection(Sunshine.COLLECTION_PATHNAME)
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
                                isProperSunshineFound = recordChoreIntoPromisingSunshine(firestore, isProperSunshineFound, document, flurr);
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

                            firestore.collection(Sunshine.COLLECTION_PATHNAME)
                                .add(sunshine)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.i(TAG, "Success on updating Sunshine");

                                        // enqueueReviewSunshines() would clean these up, and should be called on rebooting the app
                                        // Awards can't be reliably calculated in this 'error' situation.
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
    private static boolean recordChoreIntoPromisingSunshine(FirebaseFirestore firestore, boolean isProperSunshineFound, QueryDocumentSnapshot document, Flurr flurr) {

        Sunshine sunshine = document.toObject(Sunshine.class);
        if (isProperSunshineFound) {
            // we already found a proper sunshine this must indicate a duplicate
            // Handle consolidating sunshines in TChoresService  (at less busy time)
            return isProperSunshineFound;
        }

        // add this flurr
        int indexFlurr = sunshine.getChoreIds().indexOf(flurr.getChoreId());
        if (indexFlurr >= 0) {
            DocumentReference sunshineRef = firestore.collection(Sunshine.COLLECTION_PATHNAME).document(document.getId());

            if (flurr.getText() == ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED) {
                sunshine.getChoreFlSnoozeCount().set(indexFlurr, 1 + sunshine.getChoreFlSnoozeCount().get(indexFlurr));
                sunshineRef.update(Sunshine.FIELD_CHOREFLSNOOZECOUNT, sunshine.getChoreFlSnoozeCount());
            }

            sunshine.getChoreFlState().set(indexFlurr, flurr.getText());
            sunshineRef.update(Sunshine.FIELD_CHOREFLSTATE, sunshine.getChoreFlState());

            // Check FIELD_AWARDPERFECTDAY here, and when reviewing sunshines
            sunshine.computePerfectDayAward();
            sunshineRef.update(Sunshine.FIELD_AWARDPERFECTDAY, sunshine.getAwardPerfectDay());

            sunshine.getChoreFlTimestamp().set(indexFlurr, Timestamp.now());
            sunshineRef.update(Sunshine.FIELD_CHOREFLTIME, sunshine.getChoreFlTimestamp())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Success on updating Sunshine");

                        // TODO trigger  compute awards here?
                    };
                });

            isProperSunshineFound = true;

        } else {
            Log.e(TAG, "choreId not found means this wasn't precalc-ed properly?");
            // perhaps this means we needed to consolidate Sunshines?

        }
        return isProperSunshineFound;
    }

    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    static void toast(final Context context, final CharSequence text) {
        // Helper for showing tests
        Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override public void run() {
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show(); }
            });
    }

}


