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


    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    private boolean running;
    private int width = 720;
    private int height = 1080;
    private int dpi;

    private String videoPath = "";

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
        running = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ScreenRecordBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setMediaProject(MediaProjection project) {
        mMediaProjection = project;
    }

    public boolean isRunning() {
        return running;
    }

    public void setConfig(int width, int height, int dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    public boolean startRecord() {
        if (mMediaProjection == null || running) {
            return false;
        }
        initRecorder();
        createVirtualDisplay();
        try {
            mMediaRecorder.start();
            running = true;
            return true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(this, "start 出错，录屏失败！", Toast.LENGTH_SHORT).show();
            running = false;
            return false;
        }
    }

    public boolean stopRecord() {
        if (!running) {
            return false;
        }
        running = false;
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

    private void createVirtualDisplay() {
        try {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
        } catch (Exception e) {
            Toast.makeText(this, "virtualDisplay 录屏出错！", Toast.LENGTH_SHORT).show();
        }
    }

    private void initRecorder() {
        mMediaRecorder = new MediaRecorder();
        //设置声音来源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置视频来源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //设置视频格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //设置视频储存地址
        videoPath = getSaveDirectory() + System.currentTimeMillis() + ".mp4";
        mMediaRecorder.setOutputFile(videoPath);
        //设置视频大小
        mMediaRecorder.setVideoSize(width, height);
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

    public String getSaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = SdCardUtil.getPrivateFilePath(this, "video");
            return rootDir;
        } else {
            return null;
        }
    }

}

