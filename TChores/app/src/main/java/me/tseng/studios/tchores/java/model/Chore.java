package me.tseng.studios.tchores.java.model;

import android.app.NotificationManager;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.EnumMap;

/**
 * Chore POJO.
 */
@IgnoreExtraProperties
public class Chore {

    public static final String FIELD_CITY = "city";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_PHOTO = "photo";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";
    public static final String FIELD_ADTIME = "adtime";                         // careful.  For some reason Firebase wants to decapitalize aDTime to adtime as a field name in the database.  So, leave this decapitalized....
    public static final String FIELD_RECURANCEINTERVAL = "recuranceInterval";
    public static final String FIELD_BDTIME = "bdtime";                         // Daily targeted time to fire. careful on capitalization....
    public static final String FIELD_SNOOZEMINUTES = "snoozeMinutes";           // minutes to add for snooze
    public static final String FIELD_MUSTWITHIN = "mustWithin";                 // time the chore must be completed within
    public static final String FIELD_NOTIFYWORLDAFTER = "notifyWorldAfter";     // time after which lack of completion will trigger notifying others
    public static final String FIELD_PRIORITYCHANNEL = "priorityChannel";       // the type of chore completion necessity


    public static final String RESTAURANT_URI_PREFIX = "restaurant:";

    private String name;
    private String city;
    private String category;
    private String photo;   // This is now the app resource id of a PNG file that will be used in the ImageView for this object (not a URL)
    private int price;
    private int numRatings;
    private double avgRating;
    private String aDTime;
    private RecuranceInterval recuranceInterval;
    private String bDTime;
    private int snoozeMinutes;
    private int mustWithin;
    private int notifyWorldAfter;
    private PriorityChannel priorityChannel;


    public enum RecuranceInterval {
        HOURLY, DAILY, WEEKLY, WEEKDAYS, WEEKENDS, BIWEEKLY, MONTHLY
    };

    public enum PriorityChannel {
        NORMAL, IMPORTANT2SELF, IMPORTANT2OTHERS, CRITICAL
    };

    public static final String PriorityChannelDescription(PriorityChannel pc) {
        String d;
        switch (pc) {
            case NORMAL:
                d = "Normal chore priority";
                break;
            case IMPORTANT2SELF:
                d = "Priority with importance to myself";
                break;
            case IMPORTANT2OTHERS:
                d = "Priority with importance to someone else";
                break;
            case CRITICAL:
                d = "Critical importance to do this chore";
                break;
            default:
                d = "APriority";
                break;
        }
        return d;
    }

    public static final int PriorityChannelImportance(PriorityChannel pc) {
        int d;
        switch (pc) {
            case NORMAL:
                d = NotificationManager.IMPORTANCE_DEFAULT;
                break;
            case IMPORTANT2SELF:
                d = NotificationManager.IMPORTANCE_HIGH;
                break;
            case IMPORTANT2OTHERS:
                d = NotificationManager.IMPORTANCE_DEFAULT;
                break;
            case CRITICAL:
                d = NotificationManager.IMPORTANCE_HIGH;  // note IMPORTANCE_MAX is apparently unused.
                break;
            default:
                d = NotificationManager.IMPORTANCE_DEFAULT;
                break;
        }
        return d;
    }


    public Chore() {}

    public Chore(String name, String city, String category, String photo,
                 int price, int numRatings, double avgRating, String aDTime, RecuranceInterval recuranceInterval, String bDTime, int snoozeMinutes, int mustWithin, int notifyWorldAfter, PriorityChannel priorityChannel) {
        this.name = name;
        this.city = city;
        this.category = category;
        this.photo = photo;
        this.price = price;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
        this.aDTime = aDTime;
        this.recuranceInterval = recuranceInterval;
        this.bDTime = bDTime;
        this.snoozeMinutes = snoozeMinutes;
        this.mustWithin = mustWithin;
        this.notifyWorldAfter = notifyWorldAfter;
        this.priorityChannel = priorityChannel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public String getADTime() {
        return aDTime;
    }

    public void setADTime(String aDTime) {
        this.aDTime = aDTime;
    }

   @Exclude
    public RecuranceInterval getRecuranceIntervalAsEnum(){
        return recuranceInterval;
    }

    // these methods are just a Firebase 9.0.0 hack to handle the enum
    public String getRecuranceInterval(){
        if (recuranceInterval == null){
            return null;
        } else {
            return recuranceInterval.name();
        }
    }

    public void setRecuranceInterval(String recuranceIntervalString){
        if (recuranceIntervalString == null){
            this.recuranceInterval = null;
        } else {
            this.recuranceInterval = RecuranceInterval.valueOf(recuranceIntervalString);
        }
    }

    public String getBDTime() {
        return bDTime;
    }

    public void setBDTime(String bDTime) {
        this.bDTime = bDTime;
    }

    public int getSnoozeMinutes() {
        return snoozeMinutes;
    }

    public void setSnoozeMinutes(int snoozeMinutes) {
        this.snoozeMinutes = snoozeMinutes;
    }

    public int getMustWithin() {
        return mustWithin;
    }

    public void setMustWithin(int mustWithin) {
        this.mustWithin = mustWithin;
    }

    public int getNotifyWorldAfter() {
        return notifyWorldAfter;
    }

    public void setNotifyWorldAfter(int notifyWorldAfter) { this.notifyWorldAfter = notifyWorldAfter; }

    @Exclude
    public PriorityChannel getPriorityTypeAsEnum(){
        return priorityChannel;
    }

    // these methods are just a Firebase 9.0.0 hack to handle the enum
    public String getPriorityChannel(){
        if (priorityChannel == null){
            return null;
        } else {
            return priorityChannel.name();
        }
    }

    public void setPriorityChannel(String priorityTypeString){
        if (priorityTypeString == null){
            this.priorityChannel = null;
        } else {
            this.priorityChannel = PriorityChannel.valueOf(priorityTypeString);
        }
    }

}
