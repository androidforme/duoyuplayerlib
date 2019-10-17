package com.wangduoyu.lib.duoyuplayerlib.utils;

import android.util.Log;

import com.wangduoyu.lib.duoyuplayerlib.BuildConfig;

public class DuoYuLog {
    public static String TAG = "DuoYuLog";
    public static boolean isLog = BuildConfig.DEBUG;

    /**
     * 仅打印高于此等级的日志
     */
    private final static int logLevel = Log.VERBOSE;

    private static DuoYuLog mLogUtil;

    private DuoYuLog() {

    }

    public static DuoYuLog getLog() {
        if (mLogUtil == null) {
            mLogUtil = new DuoYuLog();
        }
        return mLogUtil;
    }

    /**
     * The Log Level:i
     *
     * @param str
     */
    public static void i(Object str) {
        if (BuildConfig.DEBUG && logLevel <= Log.INFO) {
            String name = getFunctionName();
            if (name != null) {
                Log.i(TAG, name + " - " + str);
            } else {
                Log.i(TAG, str.toString());
            }
        }
    }

    public static void d() {
        d("");
    }

    /**
     * The Log Level:d
     *
     * @param str
     */
    public static void d(Object str) {
        if (BuildConfig.DEBUG && logLevel <= Log.DEBUG) {
            String name = getFunctionName();
            if (name != null) {
                Log.d(TAG, name + " - " + str);
            } else {
                Log.d(TAG, str.toString());
            }
        }
    }

    /**
     * The Log Level:V
     *
     * @param str
     */
    public static void v(Object str) {
        if (BuildConfig.DEBUG && logLevel <= Log.VERBOSE) {
            String name = getFunctionName();
            if (name != null) {
                Log.v(TAG, name + " - " + str);
            } else {
                Log.v(TAG, str.toString());
            }
        }
    }

    /**
     * The Log Level:w
     *
     * @param str
     */
    public static void w(Object str) {
        if (BuildConfig.DEBUG && logLevel <= Log.WARN) {
            String name = getFunctionName();
            if (name != null) {
                Log.w(TAG, name + " - " + str);
            } else {
                Log.w(TAG, str.toString());
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param str
     */
    public static void e(Object str) {
        if (logLevel <= Log.ERROR) {
            String name = getFunctionName();
            if (name != null) {
                Log.e(TAG, name + " - " + str);
            } else {
                Log.e(TAG, str.toString());
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param e
     */
    public static void e(Exception e) {
        try {
            if (logLevel <= Log.ERROR) {
                String name = getFunctionName();
                if (name != null) {
                    Log.e(TAG, name + " - " + e.toString());
                } else {
                    Log.e(TAG, e.toString());
                }
            }
        } catch (Exception e1) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * The Log Level:e
     *
     * @param log
     * @param tr
     */
    public static void e(String log, Throwable tr) {
        String line = getFunctionName();
        Log.e(TAG, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + line + ":] " +
                log + "\n", tr);
    }

    /**
     * Get The Current Function Name
     *
     * @return
     */
    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(getLog().getClass().getName())) {
                continue;
            }
            TAG = st.getFileName();
            return "[LineNumber:" + st.getLineNumber() + " " + st.getMethodName() + "]";
        }
        return null;
    }
}
