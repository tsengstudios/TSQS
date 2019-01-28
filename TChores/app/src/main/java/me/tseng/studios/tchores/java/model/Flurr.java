package me.tseng.studios.tchores.java.model;

import android.text.TextUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Model POJO for a flurr.
 */
public class Flurr {

    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_USERID = "userId";
    public static final String FIELD_USERNAME = "userName";
    public static final String FIELD_FLURR = "flurr";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_CHOREID = "choreId";

    private String userId;
    private String userName;
    private double flurr;
    private String text;
    private @ServerTimestamp Timestamp timestamp;
    private String choreId;

    public Flurr() {}

    public Flurr(FirebaseUser user, double flurr, String text, String choreId) {
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        if (TextUtils.isEmpty(this.userName)) {
            this.userName = user.getEmail();
        }

        this.flurr = flurr;
        this.text = text;
        this.choreId = choreId;
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

    public String getChoreId() { return choreId; }

    public void setChoreId(String rC) { this.choreId = rC; }

}
