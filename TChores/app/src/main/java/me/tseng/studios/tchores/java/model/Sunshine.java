package me.tseng.studios.tchores.java.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

import me.tseng.studios.tchores.java.ChoreDetailActivity;

/**
 * Sunshine POJO.
 */
@IgnoreExtraProperties
public class Sunshine {

    public static final String COLLECTION_PATHNAME = "sunshines";
    public static final String SUNSHINE_URI_PREFIX = "sunshine:";

    public static final String FIELD_USERID = "userId";
    public static final String FIELD_DAY = "day";
    public static final String FIELD_CHORENAMES = "choreNames";
    public static final String FIELD_CHOREIDS = "choreIds";
    public static final String FIELD_CHOREFLSTATE = "choreFlState";
    public static final String FIELD_CHOREFLTIME = "choreFlTimestamp";
    public static final String FIELD_CHOREFLSNOOZECOUNT = "choreFlSnoozeCount";
    public static final String FIELD_BPRECALCED = "bPreCalced";
    public static final String FIELD_SUMMARYSTATUS = "summaryStatus";
    public static final String FIELD_AWARDPERFECTDAY = "awardPerfectDay";


    private String userId;
    private String day;
    private List<String> choreNames;
    private List<String> choreIds;
    private List<String> choreFlState;
    private List<Timestamp> choreFlTimestamp;
    private List<Integer> choreFlSnoozeCount;
    private boolean bPreCalced = false;
    private String summaryStatus = "";
    private boolean awardPerfectDay = false;

    public static final String SUMMARYSTATUS_ = "chores accounted for"; // TODO maybe don't need this property

    public Sunshine() {
    }

    public Sunshine(String userId, String day) {
        this.userId = userId;
        this.day = day;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<String> getChoreNames() {
        return choreNames;
    }

    public void setChoreNames(List<String> choreNames) {
        this.choreNames = choreNames;
    }

    public List<String> getChoreIds() {
        return choreIds;
    }

    public void setChoreIds(List<String> choreIds) {
        this.choreIds = choreIds;
    }

    public List<String> getChoreFlState() {
        return choreFlState;
    }

    public void setChoreFlState(List<String> choreFlState) {
        this.choreFlState = choreFlState;
    }

    public List<Timestamp> getChoreFlTimestamp() {
        return choreFlTimestamp;
    }

    public void setChoreFlTimestamp(List<Timestamp> choreFlTimestamp) {
        this.choreFlTimestamp = choreFlTimestamp;
    }

    public List<Integer> getChoreFlSnoozeCount() {
        return choreFlSnoozeCount;
    }

    public void setChoreFlSnoozeCount(List<Integer> choreFlSnoozeCount) { this.choreFlSnoozeCount = choreFlSnoozeCount; }

    public boolean getBPreCalced() {
        return bPreCalced;
    }

    public void setBPreCalced(boolean bPreCalced) { this.bPreCalced = bPreCalced; }

    public String getSummaryStatus() {
        return summaryStatus;
    }

    public void setSummaryStatus(String summaryStatus) {
        this.summaryStatus = summaryStatus;
    }

    public boolean getAwardPerfectDay() {
        return awardPerfectDay;
    }

    public void setAwardPerfectDay(boolean awardPerfectDay) { this.awardPerfectDay = awardPerfectDay; }


    public void initChoreList() {
        if (choreIds == null)
            choreIds = new ArrayList<String>();
        if (choreNames == null)
            choreNames = new ArrayList<String>();
        if (choreFlState == null)
            choreFlState = new ArrayList<String>();
        if (choreFlTimestamp == null)
            choreFlTimestamp = new ArrayList<Timestamp>();
        if (choreFlSnoozeCount == null)
            choreFlSnoozeCount = new ArrayList<Integer>();
    }

    public void addChore(Chore c) {
        choreIds.add(c.getid());
        choreNames.add(c.getName());
        choreFlState.add("incomplete");     //TODO
        choreFlTimestamp.add(new Timestamp(0,0));
    }

    public void addChores(List<Chore> chores) {
        initChoreList();
        for(Chore c: chores) {
            addChore(c);
        }
    }

    public void addFirstAndOnlyFlurr(Flurr flurr) {

        if (flurr.getText().equals(ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED)) {
            choreFlSnoozeCount.add(1);
        } else {
            choreFlSnoozeCount.add(0);
        }

        choreFlState.set(0, flurr.getText());
        choreFlTimestamp.set(0, flurr.getTimestamp());
        // FIELD_AWARDPERFECTDAY  cannot be awarded on first flurr.  TODO check on recalcSunshine()

    }
}
