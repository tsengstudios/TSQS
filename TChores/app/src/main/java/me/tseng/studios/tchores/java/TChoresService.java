package me.tseng.studios.tchores.java;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.util.Map;

import me.tseng.studios.tchores.java.model.Restaurant;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;

import static me.tseng.studios.tchores.java.model.Restaurant.RESTAURANT_URI_PREFIX;

public class TChoresService extends JobIntentService {

    private static final String TAG = "TChores.TChoresService";
    private Context mServiceContext;

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
        Log.i(TAG, "Executing work.");
        String label = intent.getStringExtra("label");
        if (label == null) {
            label = "TChoresService Default Intent Label";
        }
        toast("Executing: " + label);

        setAlarms();


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
                    Log.i(TAG, "Got username: " + mCurrentUserName);

                    mFirestore.collection("restaurants")
                            .orderBy(Restaurant.FIELD_ADTIME, Query.Direction.DESCENDING)
                            .whereEqualTo(Restaurant.FIELD_CATEGORY, mCurrentUserName)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Map<String, Object> d = document.getData();

                                            String id = document.getId();
                                            String name = d.get(Restaurant.FIELD_NAME).toString();
                                            LocalDateTime ldt;
                                            try {
                                                ldt = LocalDateTime.parse(d.get(Restaurant.FIELD_ADTIME).toString());
                                            } catch (Exception e) {
                                                Log.e(TAG, "Date stored on Firebase database is badly formated.");
                                                ldt = LocalDateTime.MIN;
                                            }

                                            Log.d(TAG, "Got Restaurant: " + id +
                                                    " = " + name +
                                                    " at " + ldt.toString() +
                                                    " => " + d);


                                            Intent i2 = new Intent(mServiceContext, RestaurantDetailActivity.class);
                                            i2.setData(Uri.parse(RESTAURANT_URI_PREFIX + id));  // faked just to differentiate alarms on different restaurants
                                            i2.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, id);
                                            i2.putExtra(RestaurantDetailActivity.KEY_ACTION, RestaurantDetailActivity.ACTION_VIEW);
                                            i2.setAction(RestaurantDetailActivity.ACTION_VIEW); // Needed to differentiate Intents so Notification manager doesn't squash them together

                                            Intent i3 = new Intent(mServiceContext, NotificationChoreCompleteBR.class);
                                            i3.setData(Uri.parse(RESTAURANT_URI_PREFIX + id));  // faked just to differentiate alarms on different restaurants
                                            i3.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, id);
                                            i3.putExtra(RestaurantDetailActivity.KEY_ACTION, RestaurantDetailActivity.ACTION_COMPLETED);
                                            i3.setAction(RestaurantDetailActivity.ACTION_COMPLETED);

                                            AlarmManagerUtil.setAlarm(mServiceContext, id, i2, i3, ldt.toString(), name);

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
