package com.example.myapp.utils;

import android.content.Intent;
import android.util.Log;

import com.example.myapp.MainActivity;

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
            Log.d("DateTime Exception", e.toString());
        }

        return timeFormatted;
    }

}
