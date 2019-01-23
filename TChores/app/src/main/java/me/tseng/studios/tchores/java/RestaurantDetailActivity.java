package me.tseng.studios.tchores.java;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import me.tseng.studios.tchores.BuildConfig;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.adapter.RatingAdapter;
import me.tseng.studios.tchores.java.model.Rating;
import me.tseng.studios.tchores.java.model.Restaurant;
import me.tseng.studios.tchores.java.util.RestaurantUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RestaurantDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot> {

    private static final String TAG = "TChores.RestaurantDetailActivity";

    public static final String KEY_RESTAURANT_ID = BuildConfig.APPLICATION_ID + ".key_restaurant_id";   // Prefix for Intent Extra Keys
    public static final String KEY_ACTION = BuildConfig.APPLICATION_ID + ".key_action";

    public static final String ACTION_VIEW = BuildConfig.APPLICATION_ID + ".VIEW";                      // Prefix for Intent Action
    public static final String ACTION_COMPLETED = BuildConfig.APPLICATION_ID + ".COMPLETED";
    public static final String ACTION_COMPLETED_LOCALIZED = "Finished";
    public static final String ACTION_SNOOZED = BuildConfig.APPLICATION_ID + ".SNOOZED";
    public static final String ACTION_SNOOZED_LOCALIZED = "Snooze2minutes";
    public static final String ACTION_REFUSED = BuildConfig.APPLICATION_ID + ".REFUSED";
    public static final String ACTION_REFUSED_LOCALIZED = "Refuse";


    @BindView(R.id.restaurantImage)
    ImageView mImageView;

    @BindView(R.id.restaurantName)
    TextView mNameView;

    @BindView(R.id.restaurantRating)
    MaterialRatingBar mRatingIndicator;

    @BindView(R.id.restaurantNumRatings)
    TextView mNumRatingsView;

    @BindView(R.id.restaurantCity)
    TextView mCityView;

    @BindView(R.id.restaurantCategory)
    TextView mCategoryView;

    @BindView(R.id.restaurantPrice)
    TextView mPriceView;

    @BindView(R.id.viewEmptyRatings)
    ViewGroup mEmptyView;

    @BindView(R.id.recyclerRatings)
    RecyclerView mRatingsRecycler;


    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    private ListenerRegistration mRestaurantRegistration;
    private String mRestaurantId;

    private RatingAdapter mRatingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        ButterKnife.bind(this);

        // Get restaurant ID from extras
        mRestaurantId = getIntent().getExtras().getString(KEY_RESTAURANT_ID);
        if (mRestaurantId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID);
        }
        String actionId = getIntent().getExtras().getString(KEY_ACTION);
        if (actionId== null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_ACTION);
        }

        Log.i(TAG, "Restaurant Detail Activity  restaurant_id=" + mRestaurantId);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(mRestaurantId);

        // Get ratings
        Query ratingsQuery = mRestaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
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
        mRatingsRecycler.setAdapter(mRatingAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();

        mRatingAdapter.startListening();
        mRestaurantRegistration = mRestaurantRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        if (mRestaurantRegistration != null) {
            mRestaurantRegistration.remove();
            mRestaurantRegistration = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    /**
     * Listener for the Restaurant document ({@link #mRestaurantRef}).
     */
    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e);
            return;
        }

        onRestaurantLoaded(snapshot.toObject(Restaurant.class));
    }

    private void onRestaurantLoaded(Restaurant restaurant) {
        mNameView.setText(restaurant.getName());
        mRatingIndicator.setRating((float) restaurant.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, restaurant.getNumRatings()));
        mCityView.setText(restaurant.getCity());
        mCategoryView.setText(restaurant.getCategory());
        mPriceView.setText(RestaurantUtil.getPriceString(restaurant));

        // Background image
        String tempPhoto = restaurant.getPhoto();
        if (RestaurantUtil.isURL(tempPhoto)) {
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

        Log.i(TAG, "Restaurant Loaded  name=" + restaurant.getName());
    }

    @OnClick(R.id.restaurantButtonBack)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }


    @OnClick(R.id.fabShowEditDialog)
    public void onEditChoreClicked(View view) {
        Intent intent = new Intent(this, RestaurantEditActivity.class);
        intent.putExtra(RestaurantEditActivity.KEY_RESTAURANT_ID, mRestaurantRef.getId());

        startActivity(intent);
    }


}
