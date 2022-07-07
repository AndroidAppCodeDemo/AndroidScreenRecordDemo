package com.xiaxl.android_test;


import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;

import android.widget.Toast;

import androidx.annotation.Nullable;


import com.xiaxl.android_test.utils.SdCardUtil;

import java.io.IOException;

/**
 * 因为：录屏时候一般要进入后台，显示前台界面同时进行录屏
 */
public class ScreenRecordService extends Service {

    /**
     *
     */
    // 屏幕采集
    private MediaProjection mMediaProjection;
    // 屏幕录制
    private MediaRecorder mMediaRecorder;
    // 创建虚拟屏幕
    private VirtualDisplay mVirtualDisplay;

    /**
     * 数据
     */
    private boolean isRecording;
    private int mScreenWidth = 720;
    private int mScreenHeight = 1080;
    private int mScreenDpi;

    // 视频存储路径
    private String mVideoPath = "";

    /**
     * binder
     */
    public class ScreenRecordBinder extends Binder {
        public ScreenRecordService getScreenRecordService() {
            return ScreenRecordService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread serviceThread = new HandlerThread("service_thread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        isRecording = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ScreenRecordBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * 设置MediaProjection
     *
     * @param mediaProjection
     */
    public void setMediaProject(MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
    }

    /**
     * 是否正在录制
     *
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 设置屏幕参数
     *
     * @param width
     * @param height
     * @param dpi
     */
    public void setScreenConfig(int width, int height, int dpi) {
        this.mScreenWidth = width;
        this.mScreenHeight = height;
        this.mScreenDpi = dpi;
    }

    /**
     * 开始录制接口
     *
     * @return
     */
    public boolean startRecord() {
        if (mMediaProjection == null || isRecording) {
            return false;
        }
        // 准备MediaRecorder
        prepareMediaRecorder();
        // 创建虚拟屏幕
        createVirtualDisplay();
        // 开始屏幕录制
        startMediaRecorder();
        return isRecording;
    }

    /**
     * 停止录制
     *
     * @return
     */
    public boolean stopRecord() {
        if (!isRecording) {
            return false;
        }
        isRecording = false;
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mVirtualDisplay.release();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ScreenRecordService.this, "录屏出错,保存失败", Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(ScreenRecordService.this, "录屏完成，已保存。", Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * MediaRecorder
     */
    private void prepareMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        //设置声音来源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置视频来源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //设置视频格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //设置视频储存地址
        mVideoPath = getSaveDirectory() + "/" + System.currentTimeMillis() + ".mp4";
        mMediaRecorder.setOutputFile(mVideoPath);
        //设置视频大小
        mMediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);
        //设置视频编码
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置声音编码
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //视频码率
        mMediaRecorder.setVideoEncodingBitRate(2 * 1920 * 1080);
        mMediaRecorder.setVideoFrameRate(18);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "prepare出错，录屏失败！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建虚拟屏幕
     */
    private void createVirtualDisplay() {
        try {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainScreen", mScreenWidth, mScreenHeight, mScreenDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
        } catch (Exception e) {
            Toast.makeText(this, "virtualDisplay 录屏出错！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开始屏幕录制
     *
     * @return
     */
    private void startMediaRecorder() {
        try {
            mMediaRecorder.start();
            isRecording = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(this, "start 出错，录屏失败！", Toast.LENGTH_SHORT).show();
            isRecording = false;
        }
    }


    /**
     * 获取存储路径
     *
     * @return
     */
    public String getSaveDirectory() {
        String rootDir = SdCardUtil.getPrivateFilePath(this, "video");
        return rootDir;
    }

}

