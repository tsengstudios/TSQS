package me.tseng.studios.tchores.java;

import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.model.Flurr;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
import java.util.List;

import butterknife.OnClick;
import me.tseng.studios.tchores.java.util.FlurrUtil;

import static me.tseng.studios.tchores.java.util.ChoreUtil.getLocalDateTime;


public class ChoreAddActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private static final String TAG = "TChores.ChoreAddActivity";

    RatingBar mRatingBar;
    CalendarView mCalendarView;
    LocalDate mLocalDateCalendarView;
    EditText mEditTextTime;
    Spinner priorityChannelSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chore);

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
                mTimePicker = new TimePickerDialog(ChoreAddActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mEditTextTime.setText(String.format("%1$02d:%2$02d", selectedHour,selectedMinute));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            };
        });

        // Init PriorityChannel spinner
        ArrayAdapter pcAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, Chore.PriorityChannel.values());
        priorityChannelSpinner = (Spinner) findViewById(R.id.spinnerPriorityChannel);
        priorityChannelSpinner .setAdapter(pcAdapter);

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

        //getting priortiyChannel
        final Spinner priorityChannelSpinner = (Spinner) findViewById(R.id.spinnerPriorityChannel);
        String sPriorityChannel = priorityChannelSpinner.getSelectedItem().toString();
        Chore.PriorityChannel priorityChannel = Chore.PriorityChannel.valueOf(sPriorityChannel);


        //username
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getDisplayName();

        LocalDateTime ldt = getLocalDateTime(mLocalDateCalendarView, mEditTextTime.getText().toString());

        Chore newChore = new Chore(
                name,
                uid,
                feedbackType,
                "2131230816",
                Math.round(mRatingBar.getRating()),
                0,
                0,
                ldt.toString(),
                Chore.RecuranceInterval.DAILY,
                ldt.toString(),
                10,
                25*60,
                2*60,
                priorityChannel);


        List<Flurr> randomFlurrs = FlurrUtil.getRandomList(newChore.getNumRatings());
        newChore.setAvgRating(FlurrUtil.getAverageRating(randomFlurrs));

        batch.set(restRef, newChore);

        for (Flurr flurr : randomFlurrs) {
            batch.set(restRef.collection("ratings").document(), flurr);
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
