package me.tseng.studios.tchores.java;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Rating;
import me.tseng.studios.tchores.java.model.Restaurant;
import me.tseng.studios.tchores.java.util.RatingUtil;
import me.tseng.studios.tchores.java.util.RestaurantUtil;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static me.tseng.studios.tchores.java.RestaurantDetailActivity.KEY_RESTAURANT_ID;


public class RestaurantEditActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot> {

    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    private ListenerRegistration mRestaurantRegistration;

    private static final String TAG = "MainActivity";

    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";

 //   @BindView(R.id.restaurantImage)
 //   ImageView mImageView;

    @BindView(R.id.EReditTextName)
    EditText mNameView;

//    @BindView(R.id.restaurantRating)
//    MaterialRatingBar mRatingIndicator;

//    @BindView(R.id.restaurantNumRatings)
//    TextView mNumRatingsView;

//    @BindView(R.id.restaurantCity)
//    TextView mCityView;

    @BindView(R.id.ERspnrAssignee)
    Spinner mCategoryView;

    @BindView(R.id.ERdiffucultyBar)
    RatingBar mPriceView;

    RatingBar mRatingBar;
    CalendarView mCalendarView;
    LocalDate mLocalDateCalendarView;
    EditText mEditTextTime;

    DocumentSnapshot document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_restaurant);
        ButterKnife.bind(this);

        String restaurantId = getIntent().getExtras().getString(KEY_RESTAURANT_ID);
        if (restaurantId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID);
        }

        //Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId);


        mRestaurantRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                   if (task.isSuccessful()) {
                       document = task.getResult();
                       if (document.exists()) {
                           mNameView.setText(document.getString(Restaurant.FIELD_NAME));
                           mCategoryView.setSelection(getIndex(mCategoryView, document.getString(Restaurant.FIELD_CATEGORY)));

                           Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                       } else {
                           Log.d(TAG, "No such document");
                       }
                   } else {
                       Log.d(TAG, "get failed with ", task.getException());
                   }
               }
           });


        // Calendar Date View
        mCalendarView = (CalendarView) findViewById(R.id.ERcalendarView);
        mCalendarView.setMinDate(System.currentTimeMillis());
        mLocalDateCalendarView = LocalDate.now();
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {   // LOL   month is 0-11
                mLocalDateCalendarView = LocalDate.of(year, month+1, dayOfMonth);
            }
        });

        //  initiate the editTime edit text
        mEditTextTime = (EditText) findViewById(R.id.EReditTextTime);
        // perform click event listener on edit text
        mEditTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = LocalDateTime.now().getHour();
                int minute = LocalDateTime.now().getMinute();
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(RestaurantEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mEditTextTime.setText(String.format("%1$02d:%2$02d", selectedHour,selectedMinute));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            };
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mRestaurantRegistration = mRestaurantRef.addSnapshotListener(this);
    }
    @Override
    public void onStop() {
        super.onStop();

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

//        onRestaurantLoaded(snapshot.toObject(Restaurant.class));
    }
//
//    private void onRestaurantLoaded(Restaurant restaurant) {
//        mNameView.setText(restaurant.getName());
//        mRatingIndicator.setRating((float) restaurant.getAvgRating());
//        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, restaurant.getNumRatings()));
//        mCityView.setText(restaurant.getCity());
//        mCategoryView.setText(restaurant.getCategory());
//        mPriceView.setText(RestaurantUtil.getPriceString(restaurant));
//
//        // Background image
//        String tempPhoto = restaurant.getPhoto();
//        if (RestaurantUtil.isURL(tempPhoto)) {
//            Glide.with(mImageView.getContext())
//                    .load(tempPhoto)
//                    .into(mImageView);
//        } else {
//            try {
//                int tp = Integer.valueOf(tempPhoto);
//                mImageView.setImageResource(tp);
//            } catch (Exception e){
//                // not an int or not a resource number; use default image
//            }
//        }
//    }

    @OnClick(R.id.ERrestaurantButtonBack)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.EReditRbutton)
    public void submitRestaurant(View button) {
        DocumentReference restRef = mFirestore.collection("restaurants").document();

        //getting text input

        //getting assigned to whom data
        String feedbackType = mCategoryView.getSelectedItem().toString();

        //username
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getDisplayName();


        mRestaurantRef.update(Restaurant.FIELD_NAME, mNameView.getText().toString());
        mRestaurantRef.update(Restaurant.FIELD_CATEGORY, feedbackType)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("TAG","YAY!");
                    }
                });

//
//        Restaurant newChore = new Restaurant(
//                name,
//                uid,
//                feedbackType,
//                "d",
//                Math.round(mRatingBar.getRating()),
//                0,
//                0,
//                ldt.toString(),
//                Restaurant.RecuranceInterval.DAILY);
//
//
//        List<Rating> randomRatings = RatingUtil.getRandomList(newChore.getNumRatings());
//        newChore.setAvgRating(RatingUtil.getAverageRating(randomRatings));
//
//        batch.set(restRef, newChore);
//
//        for (Rating rating : randomRatings) {
//            batch.set(restRef.collection("ratings").document(), rating);
//        }
//
//        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    Log.d(TAG, "Write batch succeeded.");
//                } else {
//                    Log.w(TAG, "write batch failed.", task.getException());
//                }
//            }
//        });

        finish();   //return to main activity
    }
    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return 0;
    }
}
