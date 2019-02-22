package me.tseng.studios.tchores.java;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
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
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.util.AlarmManagerUtil;
import me.tseng.studios.tchores.java.util.ChoreUtil;

import static me.tseng.studios.tchores.java.util.ChoreUtil.getLocalDateTime;


public class ChoreEditActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot> {

    private FirebaseFirestore mFirestore;
    private DocumentReference mchoreRef;
    private ListenerRegistration mchoreRegistration;

    private static final String TAG = "TChores.ChoreEditActivity";

    public static final String KEY_chore_ID = "key_chore_id";

 //   @BindView(R.id.choreImage)
 //   ImageView mImageView;

    @BindView(R.id.EReditRbutton)
    Button mUpdateButton;

    @BindView(R.id.EReditTextName)
    EditText mNameView;

//    @BindView(R.id.choreRating)
//    MaterialRatingBar mRatingIndicator;

//    @BindView(R.id.choreNumRatings)
//    TextView mNumRatingsView;

//    @BindView(R.id.choreCity)
//    TextView mCityView;

//    @BindView(R.id.ERspnrAssignee)
//    Spinner mSpinnerAssignee;

    @BindView(R.id.ERspinnerPhoto)
    Spinner mSpinnerPhoto;

    @BindView(R.id.ERSpinnerPriorityChannel)
    Spinner mSpinnerPriority;

    @BindView(R.id.ERSpinnerSnoozeMinutes)
    Spinner mSpinnerSnoozeMinutes;

    @BindView(R.id.ERSpinnerBackupNotificationDelay)
    Spinner mSpinnerBackupNotificationDelay;

    @BindView(R.id.ERSpinnerCriticalBackupTime)
    Spinner mSpinnerCriticalBackupTime;

    @BindView(R.id.ERSpinnerRecurrenceInterval)
    Spinner mSpinnerRecurrence;

    @BindView(R.id.ERdiffucultyBar)
    RatingBar mPriceView;

    @BindView(R.id.checkboxDebugTime)
    CheckBox mCheckboxDebugTime;

    RatingBar mRatingBar;
    CalendarView mCalendarView;
    LocalDate mLocalDateOnCalendarView;
    EditText mEditTextTime;

    DocumentSnapshot document;
    String mchoreId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chore);
        ButterKnife.bind(this);

        mchoreId = getIntent().getExtras().getString(KEY_chore_ID);
        if (mchoreId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_chore_ID);
        }

        //Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the chore
        mchoreRef = mFirestore.collection("chores").document(mchoreId);

        final Context context = this;
        mchoreRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        Chore chore = document.toObject(Chore.class);
                        mNameView.setText(chore.getName());
                        mSpinnerPhoto.setSelection(getIndex(mSpinnerPhoto, chore.getPhoto()));
                        mSpinnerPriority.setSelection(getIndex(mSpinnerPriority, chore.getPriorityChannel()));
                        mSpinnerRecurrence.setSelection(getIndex(mSpinnerRecurrence, chore.getRecurrenceInterval()));
                        mSpinnerSnoozeMinutes.setSelection(getIndex(mSpinnerSnoozeMinutes,
                                ChoreUtil.getSnoozeMinutesNearestLabel(context, chore.getSnoozeMinutes())));
                        mSpinnerBackupNotificationDelay.setSelection(getIndex(mSpinnerBackupNotificationDelay,
                                ChoreUtil.getBackupNotificationDelayNearestLabel(context, chore.getBackupNotificationDelay())));
                        mSpinnerCriticalBackupTime.setSelection(getIndex(mSpinnerCriticalBackupTime,
                                ChoreUtil.getCriticalBackupTimeNearestLabel(context, chore.getCriticalBackupTime())));


                        LocalDateTime ldt = AlarmManagerUtil.localDateTimeFromString(chore.getBDTime());

                        mLocalDateOnCalendarView = ldt.toLocalDate();
                        mCalendarView.setDate(ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

                        mEditTextTime.setText(String.format("%1$02d:%2$02d", ldt.getHour(), ldt.getMinute()));

                        mPriceView.setRating(chore.getPrice());

                        mUpdateButton.setEnabled(true);

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
                mTimePicker = new TimePickerDialog(ChoreEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mEditTextTime.setText(String.format("%1$02d:%2$02d", selectedHour,selectedMinute));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            };
        });

        // initiate the spinner for chore Photo, PriorityChannel and RecurrenceInterval
        ChoreImageAdapter adapter = ChoreImageAdapter.getChoreImageAdapter(this);
        mSpinnerPhoto.setAdapter(adapter);
        ArrayAdapter pcAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, Chore.PriorityChannel.values());
        mSpinnerPriority.setAdapter(pcAdapter);
        ArrayAdapter riAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, Chore.RecurrenceInterval.values());
        mSpinnerRecurrence.setAdapter(riAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        mchoreRegistration = mchoreRef.addSnapshotListener(this);
    }
    @Override
    public void onStop() {
        super.onStop();

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

//        onchoreLoaded(snapshot.toObject(Chore.class));
    }
//
//    private void onchoreLoaded(Chore chore) {
//        mNameView.setText(chore.getName());
//        mRatingIndicator.setFlurr((float) chore.getAvgRating());
//        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, chore.getNumRatings()));
//        mCityView.setText(chore.getCity());
//        mSpinnerAssignee.setText(chore.getUuid());
//        mPriceView.setText(ChoreUtil.getPriceString(chore));
//
//        // Background image
//        String tempPhoto = chore.getPhoto();
//        if (ChoreUtil.isURL(tempPhoto)) {
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

    @OnClick(R.id.ERchoreButtonBack)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.EReditRbutton)
    public void submitchore(View button) {

        //getting text input
        LocalDateTime ldt = getLocalDateTime(mLocalDateOnCalendarView, mEditTextTime.getText().toString());

        if (mCheckboxDebugTime.isChecked()) {
            ldt = LocalDateTime.now().plusSeconds(10);
        }

        if (ldt.isBefore(LocalDateTime.now())) {
            Snackbar.make(findViewById(android.R.id.content),
                    "You may not set time to the past.", Snackbar.LENGTH_LONG).show();
            return;
        }


        // get selected photo
        String sPhoto = mSpinnerPhoto.getSelectedItem().toString();

        // TODO MOve this code to get the username to the filter for this user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getDisplayName();

        final Context context = this;

        mchoreRef.update(Chore.FIELD_NAME, mNameView.getText().toString());
        mchoreRef.update(Chore.FIELD_ADTIME, ldt.toString());
        mchoreRef.update(Chore.FIELD_BDTIME, ldt.toString());
        mchoreRef.update(Chore.FIELD_DATEUSERLASTSET, ldt.toString());
        mchoreRef.update(Chore.FIELD_PHOTO, sPhoto);
        mchoreRef.update(Chore.FIELD_RECURRENCEINTERVAL, mSpinnerRecurrence.getSelectedItem().toString());
        mchoreRef.update(Chore.FIELD_SNOOZEMINUTES,
                        ChoreUtil.getSnoozeMinutesFromIndex(context,
                             mSpinnerSnoozeMinutes.getSelectedItemPosition()));
        mchoreRef.update(Chore.FIELD_BACKUPNOTIFICATIONDELAY,
                        ChoreUtil.getBackupNotificationDelayFromIndex(context,
                             mSpinnerBackupNotificationDelay.getSelectedItemPosition()));
        mchoreRef.update(Chore.FIELD_CRITICALBACKUPTIME,
                        ChoreUtil.getCriticalBackupTimeFromIndex(context,
                             mSpinnerCriticalBackupTime.getSelectedItemPosition()));
        mchoreRef.update(Chore.FIELD_PRIORITYCHANNEL, mSpinnerPriority.getSelectedItem().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG,"YAY!");

                        TChoresService.enqueueSetChoreAlarm(context, mchoreId);
                        TChoresService.enqueueRecalcSunshineFromToday(context); // TODO race condition?
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
