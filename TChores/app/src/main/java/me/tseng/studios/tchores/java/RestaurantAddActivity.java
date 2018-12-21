package me.tseng.studios.tchores.java;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Rating;
import me.tseng.studios.tchores.java.model.Restaurant;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDateTime;
import java.util.List;

import butterknife.OnClick;
import me.tseng.studios.tchores.java.util.RatingUtil;


public class RestaurantAddActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private static final String TAG = "MainActivity";

    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_restaurant);

        //Firestore
        mFirestore = FirebaseFirestore.getInstance();

        //ratingListener
        ratingBar = (RatingBar) findViewById(R.id.diffucultyBar);
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

        Restaurant newChore = new Restaurant(
                name,
                uid,
                feedbackType,
                "d",
                Math.round(ratingBar.getRating()),
                0,
                0,
                "2018-12-20T23:15:30",
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
