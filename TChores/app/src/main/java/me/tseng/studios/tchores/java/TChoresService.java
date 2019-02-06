package me.tseng.studios.tchores.java;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.util.Map;

import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;


public class TChoresService extends JobIntentService {

    private static final String TAG = "TChores.TChoresService";
    private Context mServiceContext;

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 888;

    /**
     * Convenience method for enqueuing work in to this service.
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

    @Override
    protected void onHandleWork(Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        Log.i(TAG, "Executing work.");
        String label = intent.getStringExtra("label");
        if (label == null) {
            label = "TChoresService Default Intent Label";
        }
        toast("Executing: " + label);

        String sChoreId = intent.getStringExtra(ChoreDetailActivity.KEY_CHORE_ID);
        setAlarms(sChoreId);


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



    private FirebaseFirestore mFirestore;

    private static final int LIMIT = 50;

    // sChoreId == null if all chores should be scanned for setting alarm
    public void setAlarms(final String sChoreId) {
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    FirebaseAuth.getInstance().removeAuthStateListener(this);
                    //do stuff

                    // Enable Firestore logging
                    FirebaseFirestore.setLoggingEnabled(true);

                    // Firestore
                    mFirestore = FirebaseFirestore.getInstance();

                    String mCurrentUserName = user.getDisplayName();
                    Log.i(TAG, "Got username: " + mCurrentUserName);


                    Query q = (sChoreId == null) ?
                            mFirestore.collection("chores")
                                    .orderBy(Chore.FIELD_ADTIME, Query.Direction.DESCENDING)
                                    .whereEqualTo(Chore.FIELD_CATEGORY, mCurrentUserName)
                            :
                            mFirestore.collection("chores").whereEqualTo(FieldPath.documentId(), sChoreId);

                    q.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        setAlmOnQDS(mServiceContext, document);

                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });


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
