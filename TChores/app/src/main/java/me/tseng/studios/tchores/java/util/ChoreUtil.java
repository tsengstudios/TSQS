package me.tseng.studios.tchores.java.util;

import android.content.Context;
import android.util.Log;

import me.tseng.studios.tchores.BuildConfig;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Chore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Utilities for chores.
 */
public class ChoreUtil {

    private static final String TAG = "TChores.ChoreUtil";

    private static final String chore_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png";
    private static final String chore_DRAWABLE_RESOURCE_FMT = "chore_png_%d";
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
     * Create a random Chore POJO.
     */
    public static Chore getRandom(Context context) {
        Chore chore = new Chore();
        Random random = new Random();

        // Cities (first elemnt is 'Any')
        String[] cities = context.getResources().getStringArray(R.array.cities);
        cities = Arrays.copyOfRange(cities, 1, cities.length);

        // Categories (first element is 'Any')
        String[] categories = context.getResources().getStringArray(R.array.categories);
        categories = Arrays.copyOfRange(categories, 1, categories.length);

        int[] prices = new int[]{1, 2, 3};

        chore.setName(getRandomName(random));
        chore.setCity(getRandomString(cities, random));
        chore.setUuid(getRandomString(categories, random));
        chore.setPhoto(getRandomImageUrl(random, context));
        chore.setPrice(getRandomInt(prices, random));
        chore.setNumRatings(random.nextInt(20));

        // Note: average rating intentionally not set

        chore.setADTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        chore.setRecurrenceInterval(randomEnum(Chore.RecurrenceInterval.class, random).name());

        chore.setBDTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        chore.setDateUserLastSet(chore.getBDTime());
        chore.setSnoozeMinutes(10);
        chore.setMustWithin(25*60);
        chore.setNotifyWorldAfter(25*60);
        chore.setPriorityChannel(randomEnum(Chore.PriorityChannel.class, random).name());

        return chore;
    }

    public static <T extends Enum<?>> T randomEnum(Class<T> clazz, Random random){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    /**
     * Get a random image.
     */
    public static String getRandomImageUrl(Random random, Context context) {
        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
        int id = random.nextInt(MAX_IMAGE_NUM) + 1;

        String resName = String.format(Locale.getDefault(), chore_DRAWABLE_RESOURCE_FMT, id);
        int ied = context.getResources().getIdentifier(resName, "drawable", BuildConfig.APPLICATION_ID);
        return Integer.toString(ied);   // BuildConfig causes   import me.tseng.studios.tchores.BuildConfig
    }

    /**
     * Get price represented as dollar signs.
     */
    public static String getPriceString(Chore chore) {
        return getPriceString(chore.getPrice());
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

    public static LocalDate LocalDateFromLocalDateTimeString(String s) {
        try {
            LocalDate ld = LocalDateTime.parse(s).toLocalDate();
            return ld;
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }


    public static int getMyNearestIndex(Context context, int sm, int intArrayResource) {
        int[] arraySM = context.getResources().getIntArray(intArrayResource);

        if (sm < arraySM[0])
            return 0;

        int retval = 0;
        for (int i = arraySM.length - 1; i >= 0; i--) {
            if (arraySM[i] <= sm) {
                retval = i;
                break;
            }
        }

        return retval;
    }

    public static String getMyNearestLabel(Context context, int sm, int stringArrayResource, int intArrayResource) {
        String[] arraySM = context.getResources().getStringArray(stringArrayResource);

        return arraySM[getMyNearestIndex(context, sm, intArrayResource)];
    }

    public static int getMyFromIndex(Context context, int index, int intArrayResource) {
        int[] arraySM = context.getResources().getIntArray(intArrayResource);

        return arraySM[index];
    }


    public static int getSnoozeMinutesNearestIndex(Context context, int sm) {
        return getMyNearestIndex(context, sm, R.array.int_snooze_minute_fixed_options);
    }
    public static String getSnoozeMinutesNearestLabel(Context context, int sm) {
        return getMyNearestLabel(context, sm, R.array.string_snooze_minute_fixed_options, R.array.int_snooze_minute_fixed_options);
    }
    public static int getSnoozeMinutesFromIndex(Context context, int index) {
        return getMyFromIndex(context, index, R.array.int_snooze_minute_fixed_options);
    }

    public static int getBackupNotificationDelayNearestIndex(Context context, int sm) {
        return getMyNearestIndex(context, sm, R.array.int_backup_notification_delay_fixed_options);
    }
    public static String getBackupNotificationDelayNearestLabel(Context context, int sm) {
        return getMyNearestLabel(context, sm, R.array.string_backup_notification_delay_fixed_options, R.array.int_backup_notification_delay_fixed_options);
    }
    public static int getBackupNotificationDelayFromIndex(Context context, int index) {
        return getMyFromIndex(context, index, R.array.int_backup_notification_delay_fixed_options);
    }

    public static int getCriticalBackupTimeNearestIndex(Context context, int sm) {
        return getMyNearestIndex(context, sm, R.array.int_critical_backup_time_fixed_options);
    }
    public static String getCriticalBackupTimeNearestLabel(Context context, int sm) {
        return getMyNearestLabel(context, sm, R.array.string_critical_backup_time_fixed_options, R.array.int_critical_backup_time_fixed_options);
    }
    public static int getCriticalBackupTimeFromIndex(Context context, int index) {
        return getMyFromIndex(context, index, R.array.int_critical_backup_time_fixed_options);
    }

}
