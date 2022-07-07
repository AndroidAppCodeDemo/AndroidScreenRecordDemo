package com.xiaxl.android_test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

import com.xiaxl.android_test.utils.DisplayScreenHelper;
import com.xiaxl.android_test.utils.PermissionUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 权限请求的 REQUEST_CODE
    private int PERMISSION_REQUEST_CODE = 101;
    // 屏幕录制请求的 REQUEST_CODE
    private int SCREEN_RECORD_REQUEST_CODE = 102;

    /**
     * 负责录屏的后台服务(录屏时候一般要进入后台，显示前台界面同时进行录屏)
     */
    private ScreenRecordService mScreenRecordService;

    /**
     * 按钮
     */
    private Button mStartBtn;
    private TextView mEndBtn;
    private TextView mTimeTv;

    /**
     *
     */
    // 用于创建MediaProjection
    private MediaProjectionManager mMediaProjectionManager;
    // 屏幕采集
    private MediaProjection mMediaProjection;
    // 屏幕、音频录制
    private MediaRecorder mMediaRecorder;
    // 创建虚拟屏幕
    private VirtualDisplay mVirtualDisplay;

    /**
     *
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScreenRecordService.ScreenRecordBinder recordBinder = (ScreenRecordService.ScreenRecordBinder) service;
            mScreenRecordService = recordBinder.getScreenRecordService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化屏幕的宽高
        DisplayScreenHelper.initScreenSize(this);
        // 6.0以上申请录音权限
        PermissionUtil.checkPermission(this, PERMISSION_REQUEST_CODE);
        //
        // 开始按钮
        mStartBtn = findViewById(R.id.start_btn);
        mStartBtn.setOnClickListener(this);
        // 结束按钮
        mEndBtn = findViewById(R.id.end_btn);
        mEndBtn.setOnClickListener(this);
        // 录制时间按钮
        mTimeTv = findViewById(R.id.record_state_tv);

        // 绑定后天的录屏服务
        bindScreenRecordService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解除service绑定关系
        unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_btn: {
                // 请求进行屏幕录制
                startScreenRecord();
                break;
            }
            case R.id.end_btn: {
                stopScreenRecord();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 录屏弹窗 用户的选择结果回调
        if (requestCode == SCREEN_RECORD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 用户同意录屏
            onScreenRecordGranted(resultCode, data);
        } else {
            Toast.makeText(getApplicationContext(), "拒绝录屏", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int temp : grantResults) {
            // 拒绝授权
            if (temp == PERMISSION_DENIED) {
                AlertDialog dialog = new AlertDialog
                        .Builder(this).setTitle("申请权限").setMessage("这些权限很重要").setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "取消", Toast.LENGTH_LONG).show();
                    }
                }).setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 进入设置页面
                        Intent intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                        MainActivity.this.startActivity(intent);
                    }
                }).create();
                dialog.show();
                break;
            }
        }
    }


    /**
     * 绑定录屏服务 Service
     */
    private void bindScreenRecordService() {
        Intent intent = new Intent(this, ScreenRecordService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }


    private void startScreenRecord() {
        if (mMediaProjection == null) {
            requestScreenRecord();
            return;
        }
        //
        if (mScreenRecordService != null && !mScreenRecordService.isRunning()) {
            mScreenRecordService.startRecord();
            Toast.makeText(MainActivity.this, "返回主屏幕进行录制", Toast.LENGTH_SHORT).show();
            goToBackground();
        }
    }

    private void stopScreenRecord() {
        if (mScreenRecordService != null && mScreenRecordService.isRunning()) {
            mScreenRecordService.stopRecord();
        }
    }


    /**
     * 弹窗 请求进行屏幕录制
     */
    private void requestScreenRecord() {
        //开启录屏请求intent
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    /**
     * 屏幕录制 用户授权
     */
    private void onScreenRecordGranted(int resultCode, Intent data) {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        //设置mediaProjection
        if (mScreenRecordService != null) {
            mScreenRecordService.setMediaProject(mMediaProjection);
            mScreenRecordService.setConfig(DisplayScreenHelper.getScreenWidth(), DisplayScreenHelper.getScreenWidth(), DisplayScreenHelper.getScreenDpi());
        }
    }

    private void goToBackground() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }


}
