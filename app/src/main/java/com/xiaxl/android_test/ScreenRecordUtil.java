package com.xiaxl.android_test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScreenRecordUtil {
    private static ScreenRecordService mScreenRecordService;

    private static List<RecordListener> s_RecordListener = new ArrayList<>();

    private static List<OnPageRecordListener> s_PageRecordListener = new ArrayList<>();

    //true,录制结束的提示语正在显示
    public static boolean s_IsRecordingTipShowing = false;

    /**
     * 录屏功能 5.0+ 的手机才能使用
     *
     * @return
     */
    public static boolean isScreenRecordEnable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void setScreenService(ScreenRecordService screenService) {
        mScreenRecordService = screenService;
    }

    /**
     * 开始录制
     */
    public static void startScreenRecord(Activity activity, int requestCode) {
        if (isScreenRecordEnable()) {
            if (mScreenRecordService != null && !mScreenRecordService.ismIsRunning()) {
                if (!mScreenRecordService.isReady()) {
                    // 获取 MediaProjectionManager
                    MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) activity.
                            getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    if (mediaProjectionManager != null) {
                        // 录屏弹窗授权
                        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
                        PackageManager packageManager = activity.getPackageManager();
                        if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                            activity.startActivityForResult(intent, requestCode);
                        } else {
                            Toast.makeText(activity, R.string.can_not_record_tip, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    mScreenRecordService.startRecord();
                }
            }
        }
    }

    /**
     * 获取用户允许录屏后，设置必要的数据
     *
     * @param resultCode
     * @param resultData
     */
    public static void setUpData(int resultCode, Intent resultData) throws Exception {
        if (isScreenRecordEnable()) {
            // 开始录屏
            if (mScreenRecordService != null && !mScreenRecordService.ismIsRunning()) {
                mScreenRecordService.setResultData(resultCode, resultData);
                mScreenRecordService.startRecord();
            }
        }
    }

    /**
     * 停止录制
     */
    public static void stopScreenRecord(Context context) {
        if (isScreenRecordEnable()) {
            if (mScreenRecordService != null && mScreenRecordService.ismIsRunning()) {
                String str = context.getString(R.string.record_video_tip);
                mScreenRecordService.stopRecord(str);
            }
        }
    }

    /**
     * 获取录制后的文件地址
     *
     * @return
     */
    public static String getScreenRecordFilePath() {

        if (isScreenRecordEnable() && mScreenRecordService != null) {
            Log.d("111", "录制文件地址==" + mScreenRecordService.getRecordFilePath());
            return mScreenRecordService.getRecordFilePath();
        }
        return null;

    }

    /**
     * 判断当前是否在录制
     *
     * @return
     */
    public static boolean isCurrentRecording() {
        if (isScreenRecordEnable() && mScreenRecordService != null) {
            return mScreenRecordService.ismIsRunning();
        }
        return false;
    }

    /**
     * true,录制结束的提示语正在显示
     *
     * @return
     */
    public static boolean isRecodingTipShow() {
        return s_IsRecordingTipShowing;
    }

    public static void setRecordingStatus(boolean isShow) {
        s_IsRecordingTipShowing = isShow;
    }


    /**
     * 系统正在录屏，app 录屏会有冲突，清理掉一些数据
     */
    public static void clearRecordElement() {

        if (isScreenRecordEnable()) {
            if (mScreenRecordService != null) {
                mScreenRecordService.clearRecordElement();
            }
        }
    }

    public static void addRecordListener(RecordListener listener) {

        if (listener != null && !s_RecordListener.contains(listener)) {
            s_RecordListener.add(listener);
        }

    }

    public static void removeRecordListener(RecordListener listener) {
        if (listener != null && s_RecordListener.contains(listener)) {
            s_RecordListener.remove(listener);
        }
    }

    public static void addPageRecordListener(OnPageRecordListener listener) {

        if (listener != null && !s_PageRecordListener.contains(listener)) {
            s_PageRecordListener.add(listener);
        }
    }

    public static void removePageRecordListener(OnPageRecordListener listener) {

        if (listener != null && s_PageRecordListener.contains(listener)) {
            s_PageRecordListener.remove(listener);
        }
    }

    public static void onPageRecordStart() {
        if (s_PageRecordListener != null && s_PageRecordListener.size() > 0) {
            for (OnPageRecordListener listener : s_PageRecordListener) {
                listener.onStartRecord();
            }
        }
    }


    public static void onPageRecordStop() {
        if (s_PageRecordListener != null && s_PageRecordListener.size() > 0) {
            for (OnPageRecordListener listener : s_PageRecordListener) {
                listener.onStopRecord();
            }
        }
    }

    public static void onPageBeforeShowAnim() {
        if (s_PageRecordListener != null && s_PageRecordListener.size() > 0) {
            for (OnPageRecordListener listener : s_PageRecordListener) {
                listener.onBeforeShowAnim();
            }
        }
    }

    public static void onPageAfterHideAnim() {
        if (s_PageRecordListener != null && s_PageRecordListener.size() > 0) {
            for (OnPageRecordListener listener : s_PageRecordListener) {
                listener.onAfterHideAnim();
            }
        }
    }

    public static void startRecord() {
        if (s_RecordListener.size() > 0) {
            for (RecordListener listener : s_RecordListener) {
                listener.onStartRecord();
            }
        }
    }

    public static void pauseRecord() {
        if (s_RecordListener.size() > 0) {
            for (RecordListener listener : s_RecordListener) {
                listener.onPauseRecord();
            }
        }
    }

    public static void resumeRecord() {
        if (s_RecordListener.size() > 0) {
            for (RecordListener listener : s_RecordListener) {
                listener.onResumeRecord();
            }
        }
    }

    public static void onRecording(String timeTip) {
        if (s_RecordListener.size() > 0) {
            for (RecordListener listener : s_RecordListener) {
                listener.onRecording(timeTip);
            }
        }
    }

    public static void stopRecord(String stopTip) {
        if (s_RecordListener.size() > 0) {
            for (RecordListener listener : s_RecordListener) {
                listener.onStopRecord(stopTip);
            }
        }
    }

    public interface RecordListener {


        void onStartRecord();

        void onPauseRecord();

        void onResumeRecord();

        void onStopRecord(String stopTip);

        void onRecording(String timeTip);
    }


    public interface OnPageRecordListener {

        void onStartRecord();

        void onStopRecord();

        void onBeforeShowAnim();

        void onAfterHideAnim();
    }


    public static void clear() {
        if (isScreenRecordEnable() && mScreenRecordService != null) {
            mScreenRecordService.clearAll();
            mScreenRecordService = null;

        }

        if (s_RecordListener != null && s_RecordListener.size() > 0) {
            s_RecordListener.clear();
        }

        if (s_PageRecordListener != null && s_PageRecordListener.size() > 0) {
            s_PageRecordListener.clear();
        }
    }
}

