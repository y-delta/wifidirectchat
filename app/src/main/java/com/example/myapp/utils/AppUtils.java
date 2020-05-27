package com.example.myapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by itrs-203 on 11/23/17.
 */

public class AppUtils {

    public static void logMe(String tag, String message) {
        Log.e(tag, message);
    }

    public static void toastMessage(Context mContext,String message)
    {
        Toast.makeText(mContext,message,Toast.LENGTH_LONG).show();
    }
}
