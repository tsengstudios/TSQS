package me.tseng.studios.tchores.java;

import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Rating;
import me.tseng.studios.tchores.java.model.Restaurant;

import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import butterknife.OnClick;
import me.tseng.studios.tchores.java.util.RatingUtil;

import static me.tseng.studios.tchores.java.util.RestaurantUtil.getLocalDateTime;


public class RestaurantAddActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private static final String TAG = "TChores.RestaurantAddActivity";

    RatingBar mRatingBar;
    CalendarView mCalendarView;
    LocalDate mLocalDateCalendarView;
    EditText mEditTextTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_restaurant);

        //Firestore
        mFirestore = FirebaseFirestore.getInstance();

        //ratingListener
        mRatingBar = (RatingBar) findViewById(R.id.diffucultyBar);

        // Calendar Date View
        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        mCalendarView.setMinDate(System.currentTimeMillis());
        mLocalDateCalendarView = LocalDate.now();
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {   // LOL   month is 0-11
                mLocalDateCalendarView = LocalDate.of(year, month+1, dayOfMonth);
            }
        });

        //  initiate the editTime edit text
        mEditTextTime = (EditText) findViewById(R.id.editTextTime);
        // perform click event listener on edit text
        mEditTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = LocalDateTime.now().getHour();
                int minute = LocalDateTime.now().getMinute();
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(RestaurantAddActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

    @OnClick(R.id.addRbutton)
    public void submitRestaurant(View button) {
        WriteBatch batch = mFirestore.batch();
        DocumentReference restRef = mFirestore.collection("restaurants").document();

        //getting text input
        final EditText nameField = (EditText) findViewById(R.id.editTextName);
        String name = nameField.getText().toString();

        //getting assigned to whom data
        final Spinner feedbackSpinner = (Spinner) findViewById(R.id.spnrAssignee);
        String feedbackType = feedbackSpinner.getSelectedItem().toString();

        //username
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getDisplayName();

        LocalDateTime ldt = getLocalDateTime(mLocalDateCalendarView, mEditTextTime.getText().toString());

        Restaurant newChore = new Restaurant(
                name,
                uid,
                feedbackType,
                "d",
                Math.round(mRatingBar.getRating()),
                0,
                0,
                ldt.toString(),
                Restaurant.RecuranceInterval.DAILY);


        List<Rating> randomRatings = RatingUtil.getRandomList(newChore.getNumRatings());
        newChore.setAvgRating(RatingUtil.getAverageRating(randomRatings));

        batch.set(restRef, newChore);

        for (Rating rating : randomRatings) {
            batch.set(restRef.collection("ratings").document(), rating);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch succeeded.");
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });

        finish();   //return to main activity
    }

}
