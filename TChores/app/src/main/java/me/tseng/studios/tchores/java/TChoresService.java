package me.tseng.studios.tchores.java;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import me.tseng.studios.tchores.java.model.Restaurant;

public class TChoresService extends JobIntentService {

    private static final String TAG = "TChores.TChoresService";

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, TChoresService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        Log.i(TAG, "Executing work: " + intent);
        String label = intent.getStringExtra("label");
        if (label == null) {
            label = "TChoresService Default Intent Label";
        }
        toast("Executing: " + label);
        for (int i = 0; i < 5; i++) {
            Log.i(TAG, "Running service " + (i + 1)
                    + "/5 @ " + SystemClock.elapsedRealtime());
            try {

                // TODO this piece
                Thread.sleep(1000);



            } catch (InterruptedException e) {
            }
        }
        Log.i(TAG, "Completed service @ " + SystemClock.elapsedRealtime());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast("TChoresService Destroyed.");
    }



    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private static final int LIMIT = 50;

    public void setAlarms() {
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

                    // Get ${LIMIT} restaurants
                    mQuery = mFirestore.collection("restaurants")
                            .orderBy(Restaurant.FIELD_ADTIME, Query.Direction.DESCENDING)
                            .whereEqualTo(Restaurant.FIELD_CATEGORY, mCurrentUserName)
                            .limit(LIMIT);


                }else{
                    //do nothing

                }
            }
        });


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


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
