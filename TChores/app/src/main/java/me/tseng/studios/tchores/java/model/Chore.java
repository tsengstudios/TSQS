package me.tseng.studios.tchores.java.model;

import android.app.NotificationManager;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.type.DayOfWeek;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import me.tseng.studios.tchores.java.util.ChoreUtil;

/**
 * Chore POJO.
 */
@IgnoreExtraProperties
public class Chore {

    public static final String COLLECTION_PATHNAME = "chores";
    public static final String FIELD_CITY = "city";
    public static final String FIELD_UUID = "uuid";
    public static final String FIELD_PHOTO = "photo";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";
    public static final String FIELD_ADTIME = "adtime";                         // careful.  For some reason Firebase wants to decapitalize aDTime to adtime as a field name in the database.  So, leave this decapitalized....
    public static final String FIELD_RECURRENCEINTERVAL = "recurrenceInterval";
    public static final String FIELD_BDTIME = "bdtime";                         // Daily targeted time to fire. careful on capitalization....
    public static final String FIELD_DATEUSERLASTSET = "dateUserLastSet";       // datetime the user last edited this chore
    public static final String FIELD_SNOOZEMINUTES = "snoozeMinutes";           // minutes to add for snooze
    public static final String FIELD_MUSTWITHIN = "mustWithin";                 // time the chore must be completed within
    public static final String FIELD_PRIORITYCHANNEL = "priorityChannel";       // the type of chore completion necessity
    public static final String FIELD_BACKUPNOTIFICATIONDELAY = "backupNotificationDelay";   // time before backup notification fires.
    public static final String FIELD_CRITICALBACKUPTIME = "criticalBackupTime"; // time before critical backup plan enacted

    public static final String CHORE_URI_PREFIX = "chore:";

    @Exclude private String id;
    private String name;
    private String city;
    private String uuid;
    private String photo;   // This is now the app resource id of a PNG file that will be used in the ImageView for this object (not a URL)
    private int price;
    private int numRatings;
    private double avgRating;
    private String aDTime;
    private RecurrenceInterval recurrenceInterval;
    private String bDTime;
    private String dateUserLastSet;
    private int snoozeMinutes;
    private int mustWithin;
    private PriorityChannel priorityChannel;
    private int backupNotificationDelay;
    private int criticalBackupTime;

    public enum RecurrenceInterval {
        HOURLY, THREETIMESADAY, DAILY, WEEKLY, WEEKDAYS, WEEKENDS, BIWEEKLY, MONTHLY, ONLY1OCCURANCE
    };

    public enum PriorityChannel {
        NORMAL, IMPORTANT2SELF, IMPORTANT2OTHERS, CRITICAL
    };


    public Chore() {}

    public Chore(String name, String city, String uuid, String photo,
                 int price, int numRatings, double avgRating, String aDTime, RecurrenceInterval recurrenceInterval, String bDTime, String dateUserLastSet, int snoozeMinutes, int mustWithin, int notifyWorldAfter, PriorityChannel priorityChannel, int backupNotificationDelay, int criticalBackupTime) {
        this.name = name;
        this.city = city;
        this.uuid = uuid;
        this.photo = photo;
        this.price = price;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
        this.aDTime = aDTime;
        this.recurrenceInterval = recurrenceInterval;
        this.bDTime = bDTime;
        this.dateUserLastSet = dateUserLastSet;
        this.snoozeMinutes = snoozeMinutes;
        this.mustWithin = mustWithin;
        this.priorityChannel = priorityChannel;
        this.backupNotificationDelay = backupNotificationDelay;
        this.criticalBackupTime = criticalBackupTime;
    }

    public String getid() {return id; }             // @Excluded  private  id

    public void setid(String id) { this.id = id; }  // @Excluded  private  id

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public RecurrenceInterval getRecurrenceIntervalAsEnum(){
        return recurrenceInterval;
    }

    // these methods are just a Firebase 9.0.0 hack to handle the enum
    public String getRecurrenceInterval(){
        if (recurrenceInterval == null){
            return RecurrenceInterval.ONLY1OCCURANCE.name();
        } else {
            return recurrenceInterval.name();
        }
    }

    public void setRecurrenceInterval(String recurrenceIntervalString){
        if (recurrenceIntervalString == null){
            this.recurrenceInterval = RecurrenceInterval.ONLY1OCCURANCE;
        } else {
            this.recurrenceInterval = RecurrenceInterval.valueOf(recurrenceIntervalString);
        }
    }

    public String getBDTime() {
        return bDTime;
    }

    public void setBDTime(String bDTime) {
        this.bDTime = bDTime;
    }

    public String getDateUserLastSet() {
        return dateUserLastSet;
    }

    public void setDateUserLastSet(String dateUserLastSet) { this.dateUserLastSet = dateUserLastSet; }

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

    public static PriorityChannel getPriorityChannelFromString(String sPriorityChannel) {
        try {
            PriorityChannel pc = PriorityChannel.valueOf(sPriorityChannel);
            return pc;
        } catch(Exception e)  {
            return PriorityChannel.NORMAL;
        }
    }


    public int getBackupNotificationDelay() {
        return backupNotificationDelay;
    }

    public void setBackupNotificationDelay(int backupNotificationDelay) {
        this.backupNotificationDelay = backupNotificationDelay;
    }

    public int getCriticalBackupTime() {
        return criticalBackupTime;
    }

    public void setCriticalBackupTime(int criticalBackupTime) {
        this.criticalBackupTime = criticalBackupTime;
    }

    public boolean isScheduledOnDate(LocalDate ld) {
        LocalDate ldThis = ChoreUtil.LocalDateFromLocalDateTimeString(bDTime);

        if (ld.isBefore(ChoreUtil.LocalDateFromLocalDateTimeString(dateUserLastSet))) {
            // assume chore did not exist before this chore was last edited. (creation date is too ambiguous given ability to edit a chore name)
            return false;
        }

        switch (recurrenceInterval) {
            case HOURLY:
                return true;

            case THREETIMESADAY:
                return true;

            case DAILY:
                return true;

            case WEEKLY:
                return (ldThis.getDayOfWeek() == ld.getDayOfWeek());

            case WEEKDAYS:
                return (!ldThis.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !ldThis.getDayOfWeek().equals(DayOfWeek.SUNDAY));

            case WEEKENDS:
                return (ldThis.getDayOfWeek().equals(DayOfWeek.SATURDAY) || ldThis.getDayOfWeek().equals(DayOfWeek.SUNDAY));

            case BIWEEKLY:
                return (ldThis.until(ld, ChronoUnit.WEEKS) % 2 == 0);

            case MONTHLY:
                return (ldThis.getDayOfMonth() == ld.getDayOfMonth());

            case ONLY1OCCURANCE:
                return (ldThis.isEqual(ld));

            default:
                throw new UnsupportedOperationException("not finished isScheduledOnDate() support for" +  recurrenceInterval.toString());
        }

    }

}
