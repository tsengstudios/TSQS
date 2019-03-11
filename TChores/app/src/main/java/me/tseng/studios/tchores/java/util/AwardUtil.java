package me.tseng.studios.tchores.java.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import me.tseng.studios.tchores.java.model.Sunshine;

public class AwardUtil {


    public static List<String> getStringsFromLocalDateList(List<LocalDate> listLD) {
        List<String> listSPD = new ArrayList<String>();
        for(LocalDate ld : listLD) {
            listSPD.add(ld.toString());
        }
        return listSPD;
    }

    public static List<LocalDate> getLocalDatesFromStringList(List<String> perfectDays) {
        List<LocalDate> listLD = new ArrayList<LocalDate>();
        for(String spd : perfectDays) {
            listLD.add(SunshineUtil.localDateFromString(spd));
        }
        return listLD;
    }

}
