package me.tseng.studios.tchores.java.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Collection;
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
    public static final String FIELD_BPRECALCED = "bpreCalced";     // careful firestore did not want to cap the 'p' fieldname
    public static final String FIELD_SUMMARYSTATUS = "summaryStatus";
    public static final String FIELD_AWARDPERFECTDAY = "awardPerfectDay";


    private String userId;
    private String day;
    private List<String> choreNames;
    private List<String> choreIds;
    private List<String> choreFlState;
    private List<Timestamp> choreFlTimestamp;
    private List<Integer> choreFlSnoozeCount;
    private boolean bpreCalced = false;
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
        return bpreCalced;
    }

    public void setBPreCalced(boolean bPreCalced) { this.bpreCalced = bPreCalced; }

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
        choreFlState.add("");     //TODO
        choreFlTimestamp.add(new Timestamp(0,0));
        choreFlSnoozeCount.add(0);
    }

    public void addChores(List<Chore> chores) {
        initChoreList();
        for(Chore c: chores) {
            addChore(c);
        }
    }

    public void removeChore(String choreId) {
        int index = choreIds.indexOf(choreId);
        if (index == -1)
            throw new RuntimeException("There should always be a chore that was asked to be removed");

        choreIds.remove(index);
        choreNames.remove(index);
        choreFlState.remove(index);
        choreFlTimestamp.remove(index);
        choreFlSnoozeCount.remove(index);
    }

    public void addFirstAndOnlyFlurr(Flurr flurr) {
        // called after addChore()
        if (flurr.getText().equals(ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED)) {
            choreFlSnoozeCount.set(0, 1);
        }

        choreFlState.set(0, flurr.getText());
        choreFlTimestamp.set(0, flurr.getTimestamp());
        // FIELD_AWARDPERFECTDAY  cannot be awarded on first flurr.  TODO check on recalcSunshine()

    }


    public boolean computePerfectDayAward() {
        boolean bNotComplete = false;
        if (bpreCalced) {
            for (String s : choreFlState) {
                if (!ChoreDetailActivity.ACTION_COMPLETED_LOCALIZED.equals(s)) {
                    bNotComplete = true;
                    break;
                }
            }
        }

        awardPerfectDay = !bNotComplete;

        return awardPerfectDay;
        // was this computation once:
//        if (!sunshine.getChoreFlState().contains(ChoreDetailActivity.ACTION_REFUSED_LOCALIZED) &&
//                !sunshine.getChoreFlState().contains(ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED)) {
//        }
    }


    public void setSCFList(Collection<SCF> collection) {
        choreIds = new ArrayList<String>();
        choreNames = new ArrayList<String>();
        choreFlState = new ArrayList<String>();
        choreFlTimestamp = new ArrayList<Timestamp>();
        choreFlSnoozeCount = new ArrayList<Integer>();

        for (SCF scf : collection) {
            choreIds.add(scf.id);
            choreNames.add(scf.name);
            choreFlState.add(scf.flstate);
            choreFlTimestamp.add(scf.fltimestamp);
            choreFlSnoozeCount.add(scf.snoozecount);
        }
    }

    // Excluded automatically in stream to Firestore.  Or rather, no Firestore document will use this child helper class
    public static class SCF {

        public String id;
        public String name;
        public String flstate;
        public Timestamp fltimestamp;
        public Integer snoozecount;

        public SCF(String id, String name, String flstate, Timestamp fltimestamp, Integer snoozecount) {
            this.id = id;
            this.name = name;
            this.flstate = flstate;
            this.fltimestamp = fltimestamp;
            this.snoozecount = snoozecount;
        }

        public SCF newSCF(Sunshine sunshine, int i) {
            return new SCF(
                    sunshine.getChoreIds().get(i),
                    sunshine.getChoreNames().get(i),
                    sunshine.getChoreFlState().get(i),
                    sunshine.getChoreFlTimestamp().get(i),
                    sunshine.getChoreFlSnoozeCount().get(i)
            );
        }

    }

}
