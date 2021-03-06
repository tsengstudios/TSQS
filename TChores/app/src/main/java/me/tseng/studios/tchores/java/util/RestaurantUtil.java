package me.tseng.studios.tchores.java.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import me.tseng.studios.tchores.BuildConfig;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Restaurant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import static android.content.res.Resources.getSystem;

/**
 * Utilities for Restaurants.
 */
public class RestaurantUtil {

    private static final String TAG = "TChores.RestaurantUtil";

    private static final String RESTAURANT_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png";
    private static final String RESTAURANT_DRAWABLE_RESOURCE_FMT = "chore_png_%d";
    private static final int MAX_IMAGE_NUM = 12;

    private static final String[] NAME_FIRST_WORDS = {
            "Foo",
            "Bar",
            "Garbage",
            "Dishes",
            "Dog",
            "Carpets",
            "HardFloors",
            "myBathroom",
            "Exercise",
    };

    private static final String[] NAME_SECOND_WORDS = {
            "Out",
            "In",
            "Vacuum",
            "Clean",
            "Rotate",
            "Clear",
            "Run",
    };

    /**
     * Create a random Restaurant POJO.
     */
    public static Restaurant getRandom(Context context) {
        Restaurant restaurant = new Restaurant();
        Random random = new Random();

        // Cities (first elemnt is 'Any')
        String[] cities = context.getResources().getStringArray(R.array.cities);
        cities = Arrays.copyOfRange(cities, 1, cities.length);

        // Categories (first element is 'Any')
        String[] categories = context.getResources().getStringArray(R.array.categories);
        categories = Arrays.copyOfRange(categories, 1, categories.length);

        int[] prices = new int[]{1, 2, 3};

        restaurant.setName(getRandomName(random));
        restaurant.setCity(getRandomString(cities, random));
        restaurant.setCategory(getRandomString(categories, random));
        restaurant.setPhoto(getRandomImageUrl(random, context));
        restaurant.setPrice(getRandomInt(prices, random));
        restaurant.setNumRatings(random.nextInt(20));

        // Note: average rating intentionally not set

        restaurant.setADTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        restaurant.setRecuranceInterval(randomEnum(Restaurant.RecuranceInterval.class, random).name());

        return restaurant;
    }

    public static <T extends Enum<?>> T randomEnum(Class<T> clazz, Random random){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    /**
     * Get a random image.
     */
    private static String getRandomImageUrl(Random random, Context context) {
        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
        int id = random.nextInt(MAX_IMAGE_NUM) + 1;

        String resName = String.format(Locale.getDefault(), RESTAURANT_DRAWABLE_RESOURCE_FMT, id);
        int ied = context.getResources().getIdentifier(resName, "drawable", BuildConfig.APPLICATION_ID);
        return Integer.toString(ied);   // BuildConfig causes   import me.tseng.studios.tchores.BuildConfig
    }

    /**
     * Get price represented as dollar signs.
     */
    public static String getPriceString(Restaurant restaurant) {
        return getPriceString(restaurant.getPrice());
    }

    /**
     * Get price represented as dollar signs.
     */
    public static String getPriceString(int priceInt) {
        switch (priceInt) {
            case 1:
                return "$";
            case 2:
                return "$$";
            case 3:
            default:
                return "$$$";
        }
    }

    private static String getRandomName(Random random) {
        return getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random);
    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    private static int getRandomInt(int[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    public static boolean isURL(String inputUrl) {
        if (inputUrl.contains("http://"))
            return true;
        else
            return false;

//            URL url;
//            try {
//                url = new URL(inputUrl);
//            } catch (MalformedURLException e) {
//                Log.v("myApp", "bad url entered");
//            }
//            if (url == null)
//                userEnteredBadUrl();
//            else
//                continue();
    }

    public static LocalDateTime getLocalDateTime(LocalDate ldDate, String sTime) {
        LocalTime lt;

        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("H:m");
        try {
            // To get the date object from the string just called the
            // parse method and pass the time string to it. This method
            // throws ParseException if the time string is invalid.
            lt = LocalTime.parse(sTime, sdf);
        } catch (Exception e) {
            Log.e(TAG, "String not formatted well for LocalTime");
            return LocalDateTime.MIN;
        }
        return LocalDateTime.of(ldDate, lt);
    }


}
