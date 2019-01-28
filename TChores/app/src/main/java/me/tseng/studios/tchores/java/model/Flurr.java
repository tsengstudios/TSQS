package me.tseng.studios.tchores.java.model;

import android.text.TextUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Model POJO for a flurr.
 */
public class Flurr {

    private String userId;
    private String userName;
    private double flurr;
    private String text;
    private @ServerTimestamp Timestamp timestamp;

    public Flurr() {}

    public Flurr(FirebaseUser user, double flurr, String text) {
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        if (TextUtils.isEmpty(this.userName)) {
            this.userName = user.getEmail();
        }

        this.flurr = flurr;
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getFlurr() {
        return flurr;
    }

    public void setFlurr(double flurr) {
        this.flurr = flurr;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
