package com.example.myapp.utils;

import com.example.myapp.utils.AppUtils;

import java.util.Date;

public class DateUtils {

    public static String getFormattedTime(Date mDate) {

        String timeFormatted = "";
        try {

            timeFormatted = String.valueOf(mDate.getHours());

            if (mDate.getMinutes() < 10) {
                timeFormatted += ":" + "0" + String.valueOf(mDate.getMinutes());
            } else {
                timeFormatted += ":" + String.valueOf(mDate.getMinutes());
            }
        } catch (Exception e) {
            AppUtils.logMe("DateTime Exception", e.toString());
        }

        return timeFormatted;
    }

}
