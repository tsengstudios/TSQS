package me.tseng.studios.tchores.java.util;

import me.tseng.studios.tchores.java.model.Flurr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Utilities for Ratings.
 */
public class FlurrUtil {

    private static final String[] NAME_FIRST_WORDS = {
            "Bill",
            "Celeste",
            "David",
            "Ellie",
            "Fred",
            "Sue",
            "Tomas",
            "Xavier",
            "Alex",
    };

    private static final String[] NAME_SECOND_WORDS = {
            "Smith",
            "Rovet",
            "Maximu",
            "Decidu",
            "Lu",
            "Sammy",
            "Frenton",
    };

    public static final String[] REVIEW_CONTENTS = {
            // 0 - 1 stars
            "Not started.",

            // 1 - 2 stars
            "Started",

            // 2 - 3 stars
            "Half complete.",

            // 3 - 4 stars
            "Chore completed.",

            // 4 - 5 stars
            "Chore completed.  Best ever!"
    };

    /**
     * Get a list of random Flurr POJOs.
     */
    public static List<Flurr> getRandomList(int length) {
        List<Flurr> result = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            result.add(getRandom());
        }

        return result;
    }

    /**
     * Get the average rating of a List.
     */
    public static double getAverageRating(List<Flurr> flurrs) {
        double sum = 0.0;

        for (Flurr flurr : flurrs) {
            sum += flurr.getFlurr();
        }

        return sum / flurrs.size();
    }

    /**
     * Create a random Flurr POJO.
     */
    public static Flurr getRandom() {
        Flurr flurr = new Flurr();

        Random random = new Random();

        double score = random.nextDouble() * 5.0;
        String text = REVIEW_CONTENTS[(int) Math.floor(score)];

        flurr.setUserId(UUID.randomUUID().toString());
        flurr.setUserName(getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random));
        flurr.setFlurr(score);
        flurr.setText(text);

        return flurr;
    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }
}
