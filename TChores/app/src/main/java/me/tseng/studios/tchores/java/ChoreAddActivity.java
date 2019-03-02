package me.tseng.studios.tchores.java;

import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Chore;

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

import butterknife.OnClick;

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
    public void submitchore(View button) {
        final Context context = this;
        WriteBatch batch = mFirestore.batch();
        DocumentReference restRef = mFirestore.collection(Chore.COLLECTION_PATHNAME).document();
        final String futureChoreId = restRef.getId();

        //getting text input
        final EditText nameField = (EditText) findViewById(R.id.editTextName);
        String name = nameField.getText().toString();

        //getting priortiyChannel
        String sPriorityChannel = priorityChannelSpinner.getSelectedItem().toString();
        Chore.PriorityChannel priorityChannel = Chore.PriorityChannel.valueOf(sPriorityChannel);


        //username
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        LocalDateTime ldt = getLocalDateTime(mLocalDateCalendarView, mEditTextTime.getText().toString());
        if (ldt.isBefore(LocalDateTime.now())) {
            Snackbar.make(findViewById(android.R.id.content),
                    "You may not set time to the past.", Snackbar.LENGTH_LONG).show();
            return;
        }

        Chore newChore = new Chore(
                name,
                "",
                uid,
                "2131230816",
                Math.round(mRatingBar.getRating()),
                0,
                0,
                ldt.toString(),
                Chore.RecurrenceInterval.DAILY,
                ldt.toString(),
                ldt.toString(),
                10,
                25*60,
                2*60,
                priorityChannel,
                10,
                30
        );

        batch.set(restRef, newChore);
        // removed random flurr generation
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch succeeded.");

                    TChoresService.enqueueSetChoreAlarm(context, futureChoreId);
                    TChoresService.enqueueRecalcSunshineFromToday(context); // TODO race condition?
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });

        finish();   //return to main activity
    }

}
