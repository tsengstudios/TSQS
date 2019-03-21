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
import com.google.android.gms.tasks.OnFailureListener;
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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

import me.tseng.studios.tchores.java.model.Award;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.model.Sunshine;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;
import me.tseng.studios.tchores.java.util.AwardUtil;
import me.tseng.studios.tchores.java.util.SunshineUtil;


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
    public static final String RECALC_SUNSHINE_FROM_TODAY_URI = "recalc:sunshinefromtoday";
    public static final String COMPUTE_AWARDS_URI = "compute:awards";
    public static final String KEY_PERFECTDAY = "single_perfect_date";
    public static final String KEY_PERFECTDAYLIST = "list_perfect_dates";
    private static final int SUNSHINE_LIMIT = 18;       // look back this many Sunshines

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

    static void enqueueRecalcSunshineFromToday(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(RECALC_SUNSHINE_FROM_TODAY_URI));
        enqueueWork(context, intent);
    }

    static void enqueueNewPerfectDay(Context context, LocalDate ld) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(COMPUTE_AWARDS_URI));
        intent.putExtra(KEY_PERFECTDAY, ld.toString());
        enqueueWork(context, intent);
    }

    static void enqueueNewPerfectDay(Context context, List<LocalDate> listLD) {
        listLD.sort(null);  // hope to generate more valid awards starting with oldest perfect days first
        List<String> listSPD = AwardUtil.getStringsFromLocalDateList(listLD);
        Intent intent = new Intent();
        intent.setData(Uri.parse(COMPUTE_AWARDS_URI));
        intent.putStringArrayListExtra(KEY_PERFECTDAYLIST, (ArrayList<String>) listSPD);
        enqueueWork(context, intent);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        Log.i(TAG, "Executing work.");
        String suri = intent.getDataString();
        // toast("Executing: " + suri);

        if ((suri != null)) {
            if (suri.equals(REVIEW_SUNSHINE_URI)) {
                reviewSunshines(false);
            } else if (suri.equals(RECALC_SUNSHINE_FROM_TODAY_URI)) {
                reviewSunshines(true);
            } else if (suri.equals(COMPUTE_AWARDS_URI)) {
                String sNewAwardDay = intent.getStringExtra(KEY_PERFECTDAY);
                LocalDate ldNewAwardDay = SunshineUtil.localDateFromString(sNewAwardDay);

                final List<String> stringArrayNewAwardDays = intent.getStringArrayListExtra(KEY_PERFECTDAYLIST);
                List<LocalDate> listNewAwardDayStrings = (stringArrayNewAwardDays == null)
                        ? null : AwardUtil.getLocalDatesFromStringList(stringArrayNewAwardDays);

                handleNewPerfectDay(ldNewAwardDay, listNewAwardDayStrings);

            } else
                throw new UnsupportedOperationException("This intent data work is not supported.");

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
        // toast("TChoresService Destroyed.");
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

    public void reviewSunshines(final boolean needsRecalcFuture) {
        final OnCompleteListener<QuerySnapshot> onCompleteListener = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qsChores = task.getResult();  // get chores
                    reviewSunshineWithChores(user.getUid(), qsChores, needsRecalcFuture);
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

                    String mCurrentUserUid = user.getUid();
                    Log.i(TAG, "Got username: " + mCurrentUserUid);


                    Query q = (sChoreId == null) ?
                            mFirestore.collection(Chore.COLLECTION_PATHNAME)
                                    .orderBy(Chore.FIELD_ADTIME, Query.Direction.DESCENDING)
                                    .whereEqualTo(Chore.FIELD_UUID, mCurrentUserUid)
                            :
                            mFirestore.collection(Chore.COLLECTION_PATHNAME).whereEqualTo(FieldPath.documentId(), sChoreId);

                    q.get()
                        .addOnCompleteListener(onCompleteListener);


                }else{
                    // TODO try to logon and try this again
                    Log.e(TAG, "TODO try to logon and try this again");

                }
            }
        });

    }

    private static void setAlmOnQDS(Context mServiceContext, QueryDocumentSnapshot document) {
        Chore chore = document.toObject(Chore.class);
        chore.setid(document.getId());

        Log.d(TAG, "Got Chore: " + chore.getid() +
                " = " + chore.getName() +
                " at " + chore.getADTime() +
                " => " + chore.toString());

        AlarmManagerUtil.setAlarm(mServiceContext, chore);

    }


    private void reviewSunshineWithChores(final String userId, final QuerySnapshot qsChores, final boolean needsRecalcFuture) {
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

                            // Sunshines that were newly awarded perfect days can be enqueued for awards.
                            List<LocalDate> listNewPerfectDays = new ArrayList<>();

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

                                boolean bNewlyAwardedPerfectDay = false;
                                if (!sunshine.getBPreCalced()) {
                                    Sunshine pcSunshine = preCalcSunshine(userId, ldSunshine, qsChores);

                                    sunshine = mergeSunshines(pcSunshine, sunshine);

                                    bNewlyAwardedPerfectDay = sunshine.computePerfectDayAward();  // compute just before listDS2Update.add()
                                    listDS2Update.add(new Tuple2<>(listDS[i], sunshine));
                                } else {
                                    if (needsRecalcFuture && !ldSunshine.isBefore(LocalDate.now()) ) {

                                        if (ldSunshine.isEqual(LocalDate.now())) {
                                            sunshine = recalcSunshineToday(sunshine, ldSunshine, qsChores);
                                        } else {  // future sunshine
                                            sunshine = preCalcSunshine(userId, ldSunshine, qsChores);  // replace current Sunshine with new precalc one
                                        }

                                        bNewlyAwardedPerfectDay = sunshine.computePerfectDayAward();  // compute just before listDS2Update.add()
                                        listDS2Update.add(new Tuple2<>(listDS[i], sunshine));

                                    } else if (flagMergedIntoThisOne) {
                                        // this merged but not precalced sunshine ref needs updating
                                        bNewlyAwardedPerfectDay = sunshine.computePerfectDayAward();  // compute just before listDS2Update.add()
                                        listDS2Update.add(new Tuple2<>(listDS[i], sunshine));
                                    }
                                }

//                                if (bNewlyAwardedPerfectDay) {
//                                    listNewPerfectDays.add(ldSunshine);
//                                }
                                if (sunshine.getAwardPerfectDay()) {
                                    listNewPerfectDays.add(ldSunshine);
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

                            if (listNewPerfectDays.size() > 0)
                                enqueueNewPerfectDay(mServiceContext, listNewPerfectDays);

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

        // compute new AwardPerfectDay elsewhere, after done with all merging and precalcing.
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
     * DONE:    If PRECalculating expected Sunshine before the day occurs (or on the day it occurs):
     * might have chores that will get removed. or added before (or during) that day,  Manage these scenarios by
     * triggering recomputation when submitting/editing a chore.
     * If POSTCalculating expected Sunshine AFTER the day occurs:
     * might think there were chores expected, but were not created until after day (don't include)..
     * or some chores might have been deleted (don't include).
     * (WAY AFTER) BDTime or dateUserLastSet  was updated to after that day before this computation. (don't include)
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

    private Sunshine recalcSunshineToday(Sunshine sunshine, LocalDate ld, @NonNull QuerySnapshot qsChores) {
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

        // into the sunshine, add new chores now scheduled
        for(Chore c: chores) {
            int indexC = sunshine.getChoreIds().indexOf(c.getid());
            if (indexC == -1) {
                sunshine.addChore(c);
            } else {
                // keep this chore
            }
        }

        // into the sunshine, remove chores that are not in the newly semi-"precalced"
        List<String> ids2Remove = new ArrayList<>();
        for (String id : sunshine.getChoreIds()) {
            if (getIndexByIdProperty(chores, id) == -1) {
                ids2Remove.add(id); // this chore needs to be deleted from the sunshine
            }
        }
        for (String id: ids2Remove) {   // avoid ConcurrentModificationException
            sunshine.removeChore(id);
        }

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

    private int getIndexByIdProperty(List<Chore> chores, String yourString) {
        for (Chore chore: chores) {
            if (chore.getid().equals(yourString)) {
                return chores.indexOf(chore);
            }
        }
        return -1;// not in list
    }


    // either ldNewAwardDay or listNewAwardDays  is null.  Use the one valid value to actually award with the perfect day
    private void handleNewPerfectDay(final LocalDate ldNewAwardDay, final List<LocalDate> listNewAwardDays) {
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

                    String mCurrentUserUid = user.getUid();
                    Log.i(TAG, "Got username: " + mCurrentUserUid);

                    mFirestore.collection(Award.COLLECTION_PATHNAME)
                            .whereEqualTo(Award.FIELD_USERID, mCurrentUserUid)
                            .get()
                            .addOnCompleteListener(getAwardsOnCompleteListener(ldNewAwardDay, listNewAwardDays));

                }else{
                    // TODO try to find these again after logon?
                    Log.e(TAG, "TODO try to logon and try to compute awards again");

                }
            }
        });

    }

    final private OnCompleteListener<QuerySnapshot> getAwardsOnCompleteListener(final LocalDate ldNewAwardDay, final List<LocalDate> listNewAwardDays) {
        return new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    final QuerySnapshot taskResult = task.getResult();

                    Map<Award.AwardType, Tuple2<DocumentReference, Award>> mapAwardTuples = getAwardTuples(taskResult);

                    if (listNewAwardDays != null) {
                        for (LocalDate ld: listNewAwardDays) {
                            calculateAwardsWithNewPerfectDay(mapAwardTuples, ld);
                        }
                    } else
                        calculateAwardsWithNewPerfectDay(mapAwardTuples, ldNewAwardDay);

                } else {
                    Log.e(TAG, "Error getting awards for user.", task.getException());
                }
            }
        };
    }

    private Map<Award.AwardType, Tuple2<DocumentReference, Award>> getAwardTuples(QuerySnapshot taskResult) {
        Map<Award.AwardType, Tuple2<DocumentReference, Award>> retVal = new HashMap<Award.AwardType, Tuple2<DocumentReference, Award>>();

        for (final Award.AwardType awardType : Award.AwardType.values()) {
            Award award;
            QueryDocumentSnapshot qds = getQDSAwardFromQuerySnapshot(taskResult, awardType);
            DocumentReference drAward;
            if (qds == null) {
                award = createAward(awardType);
                drAward = mFirestore.collection(Award.COLLECTION_PATHNAME).document();
            } else {
                award = qds.toObject(Award.class);
                drAward = qds.getReference();
            }
            retVal.put(awardType, new Tuple2<DocumentReference, Award>(drAward, award));
        }

        return retVal;
    }

    private void calculateAwardsWithNewPerfectDay(final Map<Award.AwardType, Tuple2<DocumentReference, Award>> mapAwardTuples, LocalDate ldNewAwardDay) {
        if (ldNewAwardDay.isAfter(LocalDate.now())) {
            //  impossible to be awarded perfect day in the future
            Log.w(TAG, "Impossible to be awarded perfect day in the future: " + ldNewAwardDay.toString());
            return;
        }

        for (final Award.AwardType awardType : Award.AwardType.values()) {
            Award award = mapAwardTuples.get(awardType).f2;
            DocumentReference drAward = mapAwardTuples.get(awardType).f1;

            List<LocalDate> listDays = award.getPerfectLocalDates();
            if (listDays != null)
                listDays.sort(null);
            else
                listDays = new ArrayList<LocalDate>();

            int max_perfectdays_2keep;

            switch (awardType) {
                case PERFECTDAYS:
                    // look at previously recorded days and see if newAwardDay is a change.
                    max_perfectdays_2keep = 3;

                    if (listDays.size() > 0 && listDays.get(0).isAfter(ldNewAwardDay)) {
                        // too late to record this perfect day
                        continue;
                    } else if (listDays.contains(ldNewAwardDay)) {
                        // this was already awarded and accounted for
                        continue;
                    } else {
                        listDays.add(ldNewAwardDay);
                        listDays.sort(null);
                        while (listDays.size()> max_perfectdays_2keep)
                            listDays.remove(0);

                        award.setCountRepeats(award.getCountRepeats() + 1);
                        award.setPerfecLocalDates(listDays);

                        award.setDateLastCounted(ldNewAwardDay.toString());
                        if (listDays.size() > 1)
                            award.setFlagAwarded(true);

                    }

                    break;
                case PERFECTDAY_FIRST:
                    if (award.getFlagAwarded() == true) {
                        // all done
                        continue;
                    } else {
                        award.setDateLastCounted(ldNewAwardDay.toString());
                        award.setFlagAwarded(true);

                    }
                    break;

                case PERFECTWEEK_FIRST:
                    if (award.getFlagAwarded() == true)
                        break;
                    else {
                        // just fall thru to PerfectWeeks calculation
                    }

                case PERFECTWEEKS:
                    // look at previously recorded days and see if newAwardDay is a change.
                    max_perfectdays_2keep = 9;

                    if (listDays.size() > 0 && listDays.get(0).isAfter(ldNewAwardDay)) {
                        // too late to record this perfect day
                        continue;
                    } else if (listDays.contains(ldNewAwardDay)) {
                        // this was already awarded and accounted for
                        continue;
                    } else {
                        listDays.add(ldNewAwardDay);
                        listDays.sort(null);

                        // find first monday, then look for Tue, Wed, Thu, Fri, Sat, Sun.
                        // if failed to get that streak, look for out of order expectation
                        int iSundayOfPerfectWeek = indexSundayOfPerfectWeek(listDays);

                        if (iSundayOfPerfectWeek == 0) {
                            // not a new perfect week.  But still need to save this new perfect day

                        } else {
                            // a new perfect week
                            award.setCountRepeats(award.getCountRepeats() + 1);
                            award.setFlagAwarded(true);
                            listDays = listDays.subList(iSundayOfPerfectWeek, listDays.size());  // remove from history so it won't be counted again
                        }

                        while (listDays.size()> max_perfectdays_2keep)
                            listDays.remove(0);
                        award.setPerfecLocalDates(listDays);
                        award.setDateLastCounted(ldNewAwardDay.toString());

                    }

                    break;

                case PERFECTMONTHS:
                    // look at previously recorded days and see if newAwardDay is a change.
                    max_perfectdays_2keep = 33;

                    if (listDays.size() > 0 && listDays.get(0).isAfter(ldNewAwardDay)) {
                        // too late to record this perfect day
                        continue;
                    } else if (listDays.contains(ldNewAwardDay)) {
                        // this was already awarded and accounted for
                        continue;
                    } else {
                        listDays.add(ldNewAwardDay);
                        listDays.sort(null);

                        // find first of month
                        // if failed to get that streak, look for out of order expectation
                        int iLastDayOfPerfectMonth = indexLastDayOfPerfectMonth(listDays);

                        if (iLastDayOfPerfectMonth == 0) {
                            // not a new perfect month yet.  But still need to save this new perfect day

                        } else {
                            // a new perfect month
                            award.setCountRepeats(award.getCountRepeats() + 1);
                            award.setFlagAwarded(true);
                            listDays = listDays.subList(iLastDayOfPerfectMonth, listDays.size());  // remove from history so it won't be counted again
                        }

                        while (listDays.size() > max_perfectdays_2keep)
                            listDays.remove(0);
                        award.setPerfecLocalDates(listDays);
                        award.setDateLastCounted(ldNewAwardDay.toString());

                    }

                    break;
                case PERFECTDAY_CONTINUOUS:
                    // look at previously recorded days and see if newAwardDay is a change.
                    max_perfectdays_2keep = 2;
                    final int MIN_TRAIN_STREAK_AWARD_COUNT = 4;

                    if (listDays.size() == 0) {
                        //this is the very first perfect day
                        listDays.add(ldNewAwardDay);
                        award.setDateLastCounted(ldNewAwardDay.toString());
                        award.setCountRepeats(1);

                    } else if (listDays.get(0).isAfter(ldNewAwardDay)) {
                        // too late to record this perfect day
                        continue;

                    } else if (listDays.contains(ldNewAwardDay)) {
                        // this was already awarded and accounted for
                        continue;

                    } else if (listDays.size() == 1) {
                        // the new award day must be the next consecutive day

                        LocalDate ldA = listDays.get(0);
                        if (ldA.plusDays(1).isEqual(ldNewAwardDay)) {
                            listDays.clear();
                            listDays.add(ldNewAwardDay);

                            final int countRepeats = award.getCountRepeats() + 1;
                            award.setCountRepeats(countRepeats);

                            int countRecordStreak = award.getTarget();
                            if (countRepeats > countRecordStreak) {
                                award.setDateLastCounted(ldNewAwardDay.toString());
                                award.setTarget(countRepeats);
                            }

                            if (countRepeats >= MIN_TRAIN_STREAK_AWARD_COUNT)
                                award.setFlagAwarded(true);

                        } else if (ldA.plusDays(2).isEqual(ldNewAwardDay)) {
                            // we can wait for middle day to finish within 24 hours of the next day becoming perfect
                            listDays.add(ldNewAwardDay);

                        } else {
                            // start over countRepeats
                            listDays.clear();
                            listDays.add(ldNewAwardDay);
                            award.setCountRepeats(1);
                        }

                    } else if (listDays.size() == 2) {
                        // expect 4 possible scenarios: a1) the middle date, a2) starting a new streak of 2,
                        // b) one day after last recorded PerfectDay, c) more than one day after the last recorded PerfectDay

                        LocalDate ldLast = listDays.get(1);
                        if (ldLast.minusDays(1).isEqual(ldNewAwardDay)) {
                            if (listDays.get(0).plusDays(1).isEqual(ldNewAwardDay)) {
                                // Yay, middle date filled
                                listDays.clear();
                                listDays.add(ldLast);

                                final int countRepeats = award.getCountRepeats() + 2;
                                award.setCountRepeats(countRepeats);

                                int countRecordStreak = award.getTarget();
                                if (countRepeats > countRecordStreak) {
                                    award.setDateLastCounted(ldLast.toString());
                                    award.setTarget(countRepeats);
                                }

                                if (countRepeats >= MIN_TRAIN_STREAK_AWARD_COUNT)
                                    award.setFlagAwarded(true);
                            } else {
                                // the start of a new streak of 2
                                listDays.clear();
                                listDays.add(ldLast);
                                award.setCountRepeats(2);
                            }

                        } else if (ldLast.plusDays(1).isEqual(ldNewAwardDay)) {
                            // the start of a new streak of 2
                            listDays.clear();
                            listDays.add(ldNewAwardDay);
                            award.setCountRepeats(2);

                        } else {
                            // removes old streak possibility, allow for new middle day to be filled
                            listDays.add(ldNewAwardDay);
                            listDays.remove(0);
                            award.setCountRepeats(1);
                        }

                    } else {
                        Log.e(TAG, "Unexpected size of listdays in PERFECTDAY_CONTINUOUS");
                    }

                    award.setPerfecLocalDates(listDays);

                    break;

                default:
                    Log.e(TAG, "This AwardType is not supported yet: " + awardType.toString() );
                    continue;
            }

            // save award
            drAward.set(award).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error writing award: " + awardType, e);
                }
            });

        }
    }

    private QueryDocumentSnapshot getQDSAwardFromQuerySnapshot(QuerySnapshot taskResult, Award.AwardType awardType) {
        for (QueryDocumentSnapshot qds: taskResult) {
            if (Award.AwardType.valueOf(qds.getString(Award.FIELD_AWARDTYPE)) == awardType)
                return qds;
        }
        return null;
    }

    private Award createAward(Award.AwardType awardType) {
        Award award = new Award(user.getUid(), user.getDisplayName(), awardType);

        switch (awardType) {
            case PERFECTDAYS:
            case PERFECTWEEKS:
            case PERFECTMONTHS:
            case PERFECTDAY_FIRST:
            case PERFECTWEEK_FIRST:
            case PERFECTDAY_CONTINUOUS:


                break;
            default:
                Log.e(TAG, "This AwardType is not supported yet: " + awardType.toString() );
                return null;
        }

        return award;
    }

    private int indexLastDayOfPerfectMonth(List<LocalDate> listDays) {
        int index = 0;
        int state = 0;
        int targetDayOfMonth = 1;
        while (index < listDays.size()) {

            final LocalDate ld = listDays.get(index);
            if (ld.getDayOfMonth() == targetDayOfMonth) {
                state = targetDayOfMonth;
                targetDayOfMonth++;

                if (state == ld.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth()) {
                    // found our perfect month!
                    return index;
                }

            } else {
                if (ld.getDayOfMonth() == 1) {
                    state = 1;
                    targetDayOfMonth = 2;
                } else {
                    state = 0;
                    targetDayOfMonth = 1;
                }
            }
            index++;
        }
        return 0;
    }

    private int indexSundayOfPerfectWeek(List<LocalDate> listDays) {
        int index = 0;
        java.time.DayOfWeek state = null;
        java.time.DayOfWeek targetDayOfWeek = java.time.DayOfWeek.MONDAY;
        while (index < listDays.size()) {

            if (listDays.get(index).getDayOfWeek() == targetDayOfWeek) {
                state = targetDayOfWeek;
                targetDayOfWeek = targetDayOfWeek.plus(1);

                if (state == java.time.DayOfWeek.SUNDAY) {
                    // found our perfect week!
                    return index;
                }

            } else {
                if (listDays.get(index).getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
                    state = java.time.DayOfWeek.MONDAY;
                    targetDayOfWeek = java.time.DayOfWeek.TUESDAY;
                } else {
                    state = null;
                    targetDayOfWeek = java.time.DayOfWeek.MONDAY;
                }
            }
            index++;
        }
        return 0;
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
