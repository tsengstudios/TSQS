package me.tseng.studios.tchores.java.util;

import java.time.LocalDate;

public class SunshineUtil {


    public static LocalDate localDateFromString(String s) {
        try {
            LocalDate ld = LocalDate.parse(s);
            return ld;
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }

}
