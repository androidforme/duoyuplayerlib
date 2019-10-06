package com.wangduoyu.lib.duoyuplayerlib.utils;

import android.util.Log;

public class DyLog {
    private static final String TAG = "DyLog";
    public static boolean isLog = true;

    public static void d(String message) {
        if (isLog) {
            Log.d(TAG, message);
        }

    }

    public static void i(String message) {
        if (isLog) {
            Log.i(TAG, message);
        }

    }

    public static void e(String message, Throwable throwable) {
        if (isLog) {
            Log.e(TAG, message, throwable);
        }
    }

    public static void e(String message) {
        if (isLog) {
            Log.e(TAG, message);
        }
    }
}
