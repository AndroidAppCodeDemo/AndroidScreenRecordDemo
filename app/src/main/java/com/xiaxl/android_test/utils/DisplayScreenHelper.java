package com.xiaxl.android_test.utils;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

public class DisplayScreenHelper {
    private static int mScreenWidth;
    private static int mScreenHeight;
    private static int mScreenDpi;

    public static void initScreenSize(Activity activity){

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mScreenDpi = metrics.densityDpi;
    }

    public static int getScreenWidth(){
        return mScreenWidth;
    }

    public static int getScreenHeight(){
        return mScreenHeight;
    }

    public static int getScreenDpi(){
        return mScreenDpi;
    }
}
