package me.tseng.studios.tchores.java;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TimePicker;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.adapter.ChoreImageAdapter;
import me.tseng.studios.tchores.java.model.Restaurant;

import static me.tseng.studios.tchores.java.util.RestaurantUtil.getLocalDateTime;


public class RestaurantEditActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot> {

    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    private ListenerRegistration mRestaurantRegistration;

    private static final String TAG = "TChores.RestaurantEditActivity";

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
    LocalDate mLocalDateOnCalendarView;
    EditText mEditTextTime;
    Spinner mSpinnerPhoto;

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

                           LocalDateTime ldt;
                           try {
                               ldt = LocalDateTime.parse(document.getString(Restaurant.FIELD_ADTIME));

                               mLocalDateOnCalendarView = ldt.toLocalDate();
                               mCalendarView.setDate(ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

                               mEditTextTime.setText(String.format("%1$02d:%2$02d", ldt.getHour(),ldt.getMinute()));

                           } catch (Exception e) {
                               Log.e(TAG, "Date stored on Firebase database is badly formated.");
                           }

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
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {   // LOL   month is 0-11
                mLocalDateOnCalendarView = LocalDate.of(year, month+1, dayOfMonth);
            }
        });

        //  initiate the editTime edit text
        mEditTextTime = (EditText) findViewById(R.id.EReditTextTime);
        // perform click event listener on edit text
        mEditTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour;
                int minute;
                try {
                    LocalTime lt = LocalTime.parse(mEditTextTime.getText());
                    hour = lt.getHour();
                    minute = lt.getMinute();
                } catch (Exception e) {
                    Log.e(TAG, "Badly formatted time string means we do not get to edit this stored time");
                    hour = LocalDateTime.now().getHour();
                    minute = LocalDateTime.now().getMinute();
                }

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

        // initiate the spinner for chore Photo
        mSpinnerPhoto = (Spinner) findViewById(R.id.ERspinnerPhoto);
        ChoreImageAdapter adapter = ChoreImageAdapter.getChoreImageAdapter(this);
        mSpinnerPhoto.setAdapter(adapter);
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

        //getting text input
        LocalDateTime ldt = getLocalDateTime(mLocalDateOnCalendarView, mEditTextTime.getText().toString());

        //getting assigned to whom data
        String feedbackType = mCategoryView.getSelectedItem().toString();

        // TODO MOve this code to get the username to the filter for this user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getDisplayName();


        mRestaurantRef.update(Restaurant.FIELD_NAME, mNameView.getText().toString());
        mRestaurantRef.update(Restaurant.FIELD_ADTIME, ldt.toString());
        mRestaurantRef.update(Restaurant.FIELD_BDTIME, ldt.toString());
        mRestaurantRef.update(Restaurant.FIELD_CATEGORY, feedbackType)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG,"YAY!");
                    }
                });


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
