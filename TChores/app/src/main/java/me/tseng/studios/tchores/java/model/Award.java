package me.tseng.studios.tchores.java.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.util.AwardUtil;
import me.tseng.studios.tchores.java.util.SunshineUtil;

/**
 * Award POJO.
 */
@IgnoreExtraProperties
public class Award {

    public static final String COLLECTION_PATHNAME = "awards";
    public static final String FIELD_USERID = "userId";     // needs capital I
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_SORTINDEX = "sortIndex";
    public static final String FIELD_AWARDTYPE = "awardType";

    @Exclude private String id;

    private String userid;
    private String username;
    private boolean flagAwarded;

    private int sortIndex;
    private int countRepeats;
    private String dateLastCounted;
    private AwardType awardType;

    private List<String> perfectDays;
    private int target;
    private int approach2Target;


    public enum AwardType {
        PERFECTDAYS("Perfect Days", R.layout.item_award_perfectdays),
        PERFECTWEEKS("Perfect Weeks", R.layout.item_award),
        PERFECTMONTHS("Perfect Months", R.layout.item_award),
        //PERFECTDAY_CONTINUOUS("Perfect Days Streak"), PERFECTWEEK_CONTINUOUS("Perfect Weeks Streak"),
        PERFECTDAY_FIRST("1st\nPerfect\nDay", R.layout.item_award_firstperfectday),
        PERFECTWEEK_FIRST("First Perfect Week", R.layout.item_award)
        //, PERFECTMONTH_FIRST("First Perfect Month"),
        // PERFECTDAY_30("30 Perfect Days"), PERFECTWEEK_4("4 Perfect Weeks"), PERFECTMONTH_3("3 Perfect Months"),
        // CHORESCOMPLETED, CHORESCOMPLETED_FIRST, CHORESCOMPLETED_10, CHORESCOMPLETED_100;
        ;

        public static final AwardType values[] = values();

        private final String title1;
        private final int layoutId;

        private AwardType(final String title1, final int layoutId) {
            this.title1 = title1;
            this.layoutId = layoutId;
        }

        public String getTitle1() { return title1; }
        public int getLayoutId() { return layoutId; }

    }

    @Exclude private String awardTitle;


    public Award() {}

    public Award(String uuid, String username, AwardType awardType) {
        this.userid = uuid;
        this.username = username;
        this.flagAwarded = false;

        this.sortIndex = 1;  // TODO
        this.countRepeats = 0;
        this.dateLastCounted = LocalDate.MIN.toString();
        this.awardType = awardType;

        this.perfectDays = new ArrayList<String>();
        this.target = 0;
        this.approach2Target = 0;

    }

    public String getid() {return id; }             // @Excluded  private  id

    public void setid(String id) { this.id = id; }  // @Excluded  private  id

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userid;
    }

    public void setUserId(String userid) {
        this.userid = userid;
    }

    public boolean getFlagAwarded() {
        return flagAwarded;
    }

    public void setFlagAwarded(boolean flagAwarded) {
        this.flagAwarded = flagAwarded;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public int getCountRepeats() { return countRepeats; }

    public void setCountRepeats(int countRepeats) { this.countRepeats = countRepeats; }

    public String getDateLastCounted() { return dateLastCounted; }

    public void setDateLastCounted(String dateLastCounted) { this.dateLastCounted = dateLastCounted; }


    @Exclude
    public AwardType getAwardTypeAsEnum(){
        return awardType;
    }

    // these methods are just a Firebase 9.0.0 hack to handle the enum
    public String getAwardType(){
        if (awardType == null){
            return AwardType.PERFECTDAY_FIRST.name();
        } else {
            return awardType.name();
        }
    }

    public void setAwardType(String awardTypeString){
        if (awardTypeString == null){
            this.awardType = AwardType.PERFECTDAY_FIRST;
        } else {
            this.awardType = AwardType.valueOf(awardTypeString);
        }
    }


    public List<String> getPerfectDays() { return perfectDays; }

    @Exclude
    public List<LocalDate> getPerfectLocalDates() {
        List<LocalDate> listLD = AwardUtil.getLocalDatesFromStringList(perfectDays);
        return listLD;
    }

    public void setPerfectDays(List<String> perfectDays) { this.perfectDays = perfectDays; }

    @Exclude
    public void setPerfecLocalDates(List<LocalDate> perfectDates) {
        List<String> listSPD = AwardUtil.getStringsFromLocalDateList(perfectDates);
        this.perfectDays = listSPD;
    }


}
