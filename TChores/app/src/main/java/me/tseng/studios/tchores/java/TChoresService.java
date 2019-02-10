package me.tseng.studios.tchores.java;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DayOfWeek;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.model.Sunshine;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;


public class TChoresService extends JobIntentService {

    private static final String TAG = "TChores.TChoresService";
    private Context mServiceContext;
    private FirebaseFirestore mFirestore;
    private FirebaseUser user;

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 888;
    public static final String REVIEW_SUNSHINE_URI = "review:sunshine";
    private static final int SUNSHINE_LIMIT = 9;       // look back this many Sunshines

    /**
     * Convenience methods for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, TChoresService.class, JOB_ID, work);
    }

    static void enqueueSetAllChoreAlarms(Context context) {
        TChoresService.enqueueWork(context, new Intent());
    }

    static void enqueueSetChoreAlarm(Context context, String choreId) {
        Intent intent = new Intent();
        intent.putExtra(ChoreDetailActivity.KEY_CHORE_ID, choreId);
        enqueueWork(context, intent);
    }

    static void enqueueReviewSunshines(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(REVIEW_SUNSHINE_URI));
        enqueueWork(context, intent);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        Log.i(TAG, "Executing work.");
        String suri = intent.getDataString();
        toast("Executing: " + suri);

        if ((suri != null) && suri.equals(REVIEW_SUNSHINE_URI)) {
            reviewSunshines();
        } else {
            String sChoreId = intent.getStringExtra(ChoreDetailActivity.KEY_CHORE_ID);
            setAlarms(sChoreId);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceContext = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroying service @ " + SystemClock.elapsedRealtime());
        toast("TChoresService Destroyed.");
    }


    public void setAlarms(final String sChoreId) {
        final OnCompleteListener<QuerySnapshot> onCompleteListener = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        setAlmOnQDS(mServiceContext, document);
                    }
                } else {
                    Log.w(TAG, "Error getting chores before setting alarms.", task.getException());
                }
            }
        };
        sa(sChoreId, onCompleteListener);
    }

    public void reviewSunshines() {
        final OnCompleteListener<QuerySnapshot> onCompleteListener = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qsChores = task.getResult();  // get chores
                    reviewSunshineWithChores(user.getUid(), qsChores);
                } else {
                    Log.e(TAG, "Error getting chores before reviewing Sunshines", task.getException());
                }
            }
        };
        sa(null, onCompleteListener);
    }


    // sChoreId == null if all chores should be scanned for setting alarm
    private void sa(final String sChoreId, final OnCompleteListener<QuerySnapshot> onCompleteListener) {
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();   // TODO SECURITY ISSUE: we need to null the global user  if they logout....
                if(user != null){
                    FirebaseAuth.getInstance().removeAuthStateListener(this);
                    //do stuff

                    // Enable Firestore logging
                    FirebaseFirestore.setLoggingEnabled(true);

                    // Firestore
                    mFirestore = FirebaseFirestore.getInstance();

                    String mCurrentUserName = user.getDisplayName();        // TODO this should be using userId
                    Log.i(TAG, "Got username: " + mCurrentUserName);


                    Query q = (sChoreId == null) ?
                            mFirestore.collection("chores")
                                    .orderBy(Chore.FIELD_ADTIME, Query.Direction.DESCENDING)
                                    .whereEqualTo(Chore.FIELD_CATEGORY, mCurrentUserName)
                            :
                            mFirestore.collection("chores").whereEqualTo(FieldPath.documentId(), sChoreId);

                    q.get()
                        .addOnCompleteListener(onCompleteListener);


                }else{
                    //do nothing

                }
            }
        });

    }

    private static void setAlmOnQDS(Context mServiceContext, QueryDocumentSnapshot document) {
        Map<String, Object> d = document.getData();

        String id = document.getId();
        String name = d.get(Chore.FIELD_NAME).toString();
        LocalDateTime ldt;
        try {
            ldt = LocalDateTime.parse(d.get(Chore.FIELD_ADTIME).toString());
        } catch (Exception e) {
            Log.e(TAG, "Date stored on Firebase database is badly formated.");
            ldt = LocalDateTime.MIN;
        }

        String photo = d.get(Chore.FIELD_PHOTO).toString();
        String priorityChannel = d.get(Chore.FIELD_PRIORITYCHANNEL).toString();

        Log.d(TAG, "Got Chore: " + id +
                " = " + name +
                " at " + ldt.toString() +
                " => " + d);

        AlarmManagerUtil.setAlarm(mServiceContext, id, ldt.toString(), name, photo, priorityChannel);
    }


    private void reviewSunshineWithChores(final String userId, final QuerySnapshot qsChores) {
        if (userId == null) return; // wait to review after userId is known
        mFirestore.collection(Sunshine.COLLECTION_PATHNAME)
                .orderBy(Sunshine.FIELD_DAY, Query.Direction.DESCENDING)
                .orderBy(Sunshine.FIELD_BPRECALCED, Query.Direction.ASCENDING)
                .whereEqualTo(Sunshine.FIELD_USERID, userId)
                .limit(SUNSHINE_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot qs = task.getResult();
                            DocumentSnapshot[] listDS = qs.getDocuments().toArray(new DocumentSnapshot[0]);  // specifying qs.size() is slower!   https://stackoverflow.com/questions/4042434/converting-arrayliststring-to-string-in-java/4042464#4042464

                            // sunshines to be deleted or updated are only those going to be in these two lists
                            List<DocumentSnapshot> listDS2Delete = new ArrayList<>();
                            List<Tuple2<DocumentSnapshot, Sunshine>> listDS2Update = new ArrayList<>();

                            // this for-loop iterates thru the known sunshines
                            for (int i = 0; i < listDS.length; i ++) {
                                Sunshine sunshine = listDS[i].toObject(Sunshine.class);
                                LocalDate ldSunshine = LocalDate.parse(sunshine.getDay());

                                // Merge with the next 1 or multiple sunshines if they are the same day.
                                // This may happen when resolving a flurr without an appropriately precalced sunshine
                                boolean flagMergedIntoThisOne = false;
                                while (i + 1 < listDS.length) {
                                    Sunshine sunshine2 = listDS[i + 1].toObject(Sunshine.class);
                                    LocalDate ldSunshine2 = LocalDate.parse(sunshine2.getDay());

                                    if (ldSunshine2.isEqual(ldSunshine)) {
                                        listDS2Delete.add(listDS[i]);   // record first sunshine for deletion after merging

                                        sunshine = mergeSunshines(sunshine2, sunshine);
                                        ldSunshine = ldSunshine2;
                                        flagMergedIntoThisOne = true;
                                        i++;
                                    } else
                                        break;

                                }

                                if (!sunshine.getBPreCalced()) {
                                    Sunshine pcSunshine = preCalcSunshine(userId, ldSunshine, qsChores);

                                    sunshine = mergeSunshines(pcSunshine, sunshine);

                                    listDS2Update.add(new Tuple2<>(listDS[i], sunshine));
                                } else if (flagMergedIntoThisOne) {
                                    // this merged but not precalced sunshine ref needs updating
                                    listDS2Update.add(new Tuple2<>(listDS[i], sunshine));
                                }

                            }

                            // Delete listDS2Delete
                            for (DocumentSnapshot ds: listDS2Delete) {
                                mFirestore.collection(Sunshine.COLLECTION_PATHNAME).document(ds.getId()).delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i(TAG, "Success on deleting merged Sunshine");
                                                }
                                            }
                                        });
                            }

                            // update the remaining sunshines that need it
                            for (Tuple2<DocumentSnapshot, Sunshine> t: listDS2Update) {
                                DocumentReference dr = mFirestore.collection(Sunshine.COLLECTION_PATHNAME).document(t.getF1().getId());
                                dr.set(t.getF2())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i(TAG, "Success on updating merged Sunshine");
                                                }
                                            }
                                        });;
                            }

                            // TODO there might be sunshines between the existing ones that have not been created yet.

                            LocalDate dateLatestSunshine = (listDS.length > 0)
                                    ? LocalDate.parse(listDS[0].getString(Sunshine.FIELD_DAY))
                                    : LocalDate.now().minusDays(1);         // there are no sunshines at all yet

                            // precalc Sunshines from the latestSunshine up to the coming Sunday
                            LocalDate ldLastSunshineDayToCalc = getLdLastSunshineDayToCalc();
                            for (LocalDate ld = dateLatestSunshine.plusDays(1);
                                 !ld.isAfter(ldLastSunshineDayToCalc);
                                 ld = ld.plusDays(1)) {

                                mFirestore.collection(Sunshine.COLLECTION_PATHNAME)
                                        .add(preCalcSunshine(userId, ld, qsChores))
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i(TAG, "Success on adding new Sunshine");
                                                }
                                            }
                                        });

                            }

                        } else {
                            throw new RuntimeException("Failed Sunshine query.");
                        }

                    }
                });

    }



    private Sunshine mergeSunshines(Sunshine sunshineA, Sunshine sunshineB) {
        // verify the properties that should be equal
        if (sunshineA.getUserId() != sunshineA.getUserId()) throw new RuntimeException("Merging sunshine userId's not equal.");
        if (sunshineA.getDay() != sunshineA.getDay()) throw new RuntimeException("Merging sunshine day's not equal.");

        sunshineA.setBPreCalced(sunshineA.getBPreCalced() || sunshineB.getBPreCalced());

        Map<String, Sunshine.SCF> map = new TreeMap<String, Sunshine.SCF>();  // build a map of unique choreids to Sunshine.Chore* properties stuffed into a SCF object
        // init map with sunshineA
        for (int i = 0; i < sunshineA.getChoreIds().size(); i++) {
            map.put(sunshineA.getChoreIds().get(i),
                    new Sunshine.SCF(
                            sunshineA.getChoreIds().get(i),
                            sunshineA.getChoreNames().get(i),
                            sunshineA.getChoreFlState().get(i),
                            sunshineA.getChoreFlTimestamp().get(i),
                            sunshineA.getChoreFlSnoozeCount().get(i)
                    )
            );
        }

        // merge map with sunshineB
        for (int i = 0; i < sunshineB.getChoreIds().size(); i++) {
            String key = sunshineB.getChoreIds().get(i);
            Sunshine.SCF scfB = new Sunshine.SCF(
                    sunshineB.getChoreIds().get(i),
                    sunshineB.getChoreNames().get(i),
                    sunshineB.getChoreFlState().get(i),
                    sunshineB.getChoreFlTimestamp().get(i),
                    sunshineB.getChoreFlSnoozeCount().get(i)
            );

            // merge this SCF
            map.merge(sunshineB.getChoreIds().get(i), scfB,
                    new BiFunction<Sunshine.SCF, Sunshine.SCF, Sunshine.SCF>() {  // could be simplified to BinaryOperator<Sunshine.SCF> since we don't need to extend SCF in a returned object
                        @Override
                        public Sunshine.SCF apply(Sunshine.SCF scfA, Sunshine.SCF scfB) {
                            if (scfA.flstate.isEmpty()) { // then b is better
                                return scfB;
                            }
                            if (scfB.flstate.isEmpty()) { // then a is better
                                return scfA;
                            }
                            int totalSnoozes = scfA.snoozecount + scfB.snoozecount;
                            scfA.snoozecount = totalSnoozes;
                            scfB.snoozecount = totalSnoozes;

                            if (scfA.flstate.equals(ChoreDetailActivity.ACTION_COMPLETED_LOCALIZED))
                                return scfA;
                            if (scfB.flstate.equals(ChoreDetailActivity.ACTION_COMPLETED_LOCALIZED))
                                return scfB;
                            if (scfA.flstate.equals(ChoreDetailActivity.ACTION_REFUSED_LOCALIZED))
                                return scfA;
                            if (scfB.flstate.equals(ChoreDetailActivity.ACTION_REFUSED_LOCALIZED))
                                return scfB;

                            // don't care if either remaining possibility is ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED
                            // scfA equal to scfB
                            return scfB;
                        }
                    });
        }

        //un-Map into sunshineA
        sunshineA.setSCFList(map.values());

        // TODO compute new AwardPerfectDay here?
        sunshineA.setAwardPerfectDay(false);

        return sunshineA;
    }

    private LocalDate getLdLastSunshineDayToCalc() {
        // generate sunshine for every day from this recorded one to the end of the current week (Sunday)
        if (DayOfWeek.SUNDAY_VALUE != 7) throw new AssertionError("Assuming DayOfWeek.SUNDAY_VALUE == 7");
        return LocalDate.now().plusDays(DayOfWeek.SUNDAY_VALUE - LocalDate.now().getDayOfWeek().getValue());
    }

    /*
     * Each Sunshine will eventually be precalced = a calculation for all chores "scheduled" on that day.
     *   "scheduled" means a chore was scheduled to be done that day).
     * Note: the date that chores are finished, refused or snoozed don't matter.  That chore was
     * supposed to be done on a certain day.
     */
    private Sunshine preCalcSunshine(String userId, LocalDate ld, @NonNull QuerySnapshot qsChores) {
        // qsChores    - all Chores for user

        List<Chore> chores = new ArrayList<Chore>() {};

        // is chore x on this date?
        for (QueryDocumentSnapshot cS : qsChores) {
            Chore c = cS.toObject(Chore.class);
            if (c.isScheduledOnDate(ld)) {
                c.setid(cS.getId());    // use @Exclude  private id
                chores.add(c);
            }
        }

        Sunshine sunshine = new Sunshine(userId, ld.toString());
        sunshine.addChores(chores);
        sunshine.setBPreCalced(true);

        return sunshine;
    }

    private static <T> List<T> joinedListA(List<T> a, List<T> b) {
        a.addAll(b);
        return a;
    }

    private class Tuple2<T1,T2> {
        private T1 f1;
        private T2 f2;
        Tuple2(T1 f1, T2 f2) {
            this.f1 = f1; this.f2 = f2;
        }
        public T1 getF1() {return f1;}
        public T2 getF2() {return f2;}
    }





    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(TChoresService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

/*  https://stackoverflow.com/questions/46166328/custom-jobintentservice-onhandlework-not-called

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }*/

}
