package me.tseng.studios.tchores.java;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import me.tseng.studios.tchores.BuildConfig;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.adapter.FlurrAdapter;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.util.ChoreUtil;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ChoreDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot> {

    private static final String TAG = "TChores.ChoreDetailActivity";

    public static final String KEY_chore_ID = BuildConfig.APPLICATION_ID + ".key_chore_id";   // Prefix for Intent Extra Keys
    public static final String KEY_ACTION = BuildConfig.APPLICATION_ID + ".key_action";

    public static final String ACTION_VIEW = BuildConfig.APPLICATION_ID + ".VIEW";                      // Prefix for Intent Action
    public static final String ACTION_COMPLETED = BuildConfig.APPLICATION_ID + ".COMPLETED";
    public static final String ACTION_COMPLETED_LOCALIZED = "Finished";
    public static final String ACTION_SNOOZED = BuildConfig.APPLICATION_ID + ".SNOOZED";
    public static final String ACTION_SNOOZED_LOCALIZED = "Snooze2minutes";
    public static final String ACTION_REFUSED = BuildConfig.APPLICATION_ID + ".REFUSED";
    public static final String ACTION_REFUSED_LOCALIZED = "Refuse";


    @BindView(R.id.choreImage)
    ImageView mImageView;

    @BindView(R.id.choreName)
    TextView mNameView;

    @BindView(R.id.choreRating)
    MaterialRatingBar mRatingIndicator;

    @BindView(R.id.choreNumRatings)
    TextView mNumRatingsView;

    @BindView(R.id.choreCity)
    TextView mCityView;

    @BindView(R.id.choreCategory)
    TextView mCategoryView;

    @BindView(R.id.chorePrice)
    TextView mPriceView;

    @BindView(R.id.viewEmptyRatings)
    ViewGroup mEmptyView;

    @BindView(R.id.recyclerRatings)
    RecyclerView mRatingsRecycler;


    private FirebaseFirestore mFirestore;
    private DocumentReference mchoreRef;
    private ListenerRegistration mchoreRegistration;
    private String mchoreId;

    private FlurrAdapter mFlurrAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chore_detail);
        ButterKnife.bind(this);

        // Get chore ID from extras
        mchoreId = getIntent().getExtras().getString(KEY_chore_ID);
        if (mchoreId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_chore_ID);
        }
        String actionId = getIntent().getExtras().getString(KEY_ACTION);
        if (actionId== null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_ACTION);
        }

        Log.i(TAG, "Chore Detail Activity  chore_id=" + mchoreId);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the chore
        mchoreRef = mFirestore.collection("chores").document(mchoreId);

        // Get ratings
        Query ratingsQuery = mchoreRef
                .collection("flurrs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mFlurrAdapter = new FlurrAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };
        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mFlurrAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();

        mFlurrAdapter.startListening();
        mchoreRegistration = mchoreRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mFlurrAdapter.stopListening();

        if (mchoreRegistration != null) {
            mchoreRegistration.remove();
            mchoreRegistration = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    /**
     * Listener for the Chore document ({@link #mchoreRef}).
     */
    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "chore:onEvent", e);
            return;
        }

        onchoreLoaded(snapshot.toObject(Chore.class));
    }

    private void onchoreLoaded(Chore chore) {
        mNameView.setText(chore.getName());
        mRatingIndicator.setRating((float) chore.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, chore.getNumRatings()));
        mCityView.setText(chore.getCity());
        mCategoryView.setText(chore.getCategory());
        mPriceView.setText(ChoreUtil.getPriceString(chore));

        // Background image
        String tempPhoto = chore.getPhoto();
        if (ChoreUtil.isURL(tempPhoto)) {
            Glide.with(mImageView.getContext())
                    .load(tempPhoto)
                    .into(mImageView);
        } else {
            try {
                int tp = Integer.valueOf(tempPhoto);
                mImageView.setImageResource(tp);
            } catch (Exception e){
                // not an int or not a resource number; use default image
            }
        }

        Log.i(TAG, "Chore Loaded  name=" + chore.getName());
    }

    @OnClick(R.id.choreButtonBack)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }


    @OnClick(R.id.fabShowEditDialog)
    public void onEditChoreClicked(View view) {
        Intent intent = new Intent(this, ChoreEditActivity.class);
        intent.putExtra(ChoreEditActivity.KEY_chore_ID, mchoreRef.getId());

        startActivity(intent);
    }


}
