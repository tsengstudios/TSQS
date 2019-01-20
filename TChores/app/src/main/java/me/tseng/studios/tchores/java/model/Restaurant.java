package me.tseng.studios.tchores.java.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.time.LocalDateTime;

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
public class Restaurant {

    public static final String FIELD_CITY = "city";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";
    public static final String FIELD_ADTIME = "adtime";     // careful.  For some reason Firebase wants to decapitalize aDTime to adtime as a field name in the database.  So, leave this decapitalized....

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

    public enum RecuranceInterval {
        HOURLY, DAILY, WEEKLY, BIWEEKLY, MONTHLY
    };

    public Restaurant() {}

    public Restaurant(String name, String city, String category, String photo,
                      int price, int numRatings, double avgRating, String aDTime, RecuranceInterval recuranceInterval) {
        this.name = name;
        this.city = city;
        this.category = category;
        this.photo = photo;
        this.price = price;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
        this.aDTime = aDTime;
        this.recuranceInterval = recuranceInterval;
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
}
