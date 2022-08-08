package com.xiaxl.android_test.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtil {
    /**
     * 录音权限
     *
     * @param activity
     */
    public static void checkPermission(Activity activity, int requestCode) {
        // 6.0以上 申请录音权限
        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission =
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                //动态申请
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.SYSTEM_ALERT_WINDOW}, requestCode);
                return;
            } else {
                return;
            }
        }
        return;
    }
}

