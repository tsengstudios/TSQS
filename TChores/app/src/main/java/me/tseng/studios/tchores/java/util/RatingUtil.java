package me.tseng.studios.tchores.java.util;

import me.tseng.studios.tchores.java.model.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Utilities for Ratings.
 */
public class RatingUtil {

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
     * Get a list of random Rating POJOs.
     */
    public static List<Rating> getRandomList(int length) {
        List<Rating> result = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            result.add(getRandom());
        }

        return result;
    }

    /**
     * Get the average rating of a List.
     */
    public static double getAverageRating(List<Rating> ratings) {
        double sum = 0.0;

        for (Rating rating : ratings) {
            sum += rating.getRating();
        }

        return sum / ratings.size();
    }

    /**
     * Create a random Rating POJO.
     */
    public static Rating getRandom() {
        Rating rating = new Rating();

        Random random = new Random();

        double score = random.nextDouble() * 5.0;
        String text = REVIEW_CONTENTS[(int) Math.floor(score)];

        rating.setUserId(UUID.randomUUID().toString());
        rating.setUserName(getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random));
        rating.setRating(score);
        rating.setText(text);

        return rating;
    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }
}
