package com.xiaxl.android_test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

import com.xiaxl.android_test.utils.DisplayScreenUtil;
import com.xiaxl.android_test.utils.PermissionUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 权限请求的 REQUEST_CODE
    private int PERMISSION_REQUEST_CODE = 101;
    // 屏幕录制请求的 REQUEST_CODE
    private int SCREEN_RECORD_REQUEST_CODE = 102;

    /**
     * 负责录屏的后台服务
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
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScreenRecordService.RecordBinder recordBinder = (ScreenRecordService.RecordBinder) service;
            mScreenRecordService = recordBinder.getRecordService();
            ScreenRecordUtil.setScreenService(mScreenRecordService);
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
        DisplayScreenUtil.initScreenSize(this);
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
        mTimeTv = findViewById(R.id.record_time_tv);
        //
        startScreenRecordService();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_btn: {
                ScreenRecordUtil.startScreenRecord(this, SCREEN_RECORD_REQUEST_CODE);
                break;
            }
            case R.id.end_btn: {
                ScreenRecordUtil.stopScreenRecord(this);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 录屏弹窗 用户的选择结果回调
        if (requestCode == SCREEN_RECORD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                ScreenRecordUtil.setUpData(resultCode, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "拒绝录屏", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int temp : grantResults) {
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
     * 开启录制 Service
     */
    private void startScreenRecordService() {
        Intent intent = new Intent(this, ScreenRecordService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        //
        ScreenRecordUtil.addRecordListener(recordListener);
    }


    private ScreenRecordUtil.RecordListener recordListener = new ScreenRecordUtil.RecordListener() {
        @Override
        public void onStartRecord() {

        }

        @Override
        public void onPauseRecord() {

        }

        @Override
        public void onResumeRecord() {

        }

        @Override
        public void onStopRecord(String stopTip) {
            Toast.makeText(getApplicationContext(), stopTip, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRecording(String timeTip) {
            mTimeTv.setText(timeTip);
        }
    };


}
