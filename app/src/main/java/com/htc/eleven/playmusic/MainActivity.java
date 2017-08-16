package com.htc.eleven.playmusic;

import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.security.Permission;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String[] ReadPermission = new String[]{"android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE"};

    private final static String mMusicFIle = "test.mp3";
    private AssetFileDescriptor mFd = null;

    private Button mStart = null;
    private Button mStop = null;
    private MediaPlayer player = null;
    private Boolean mRunning = false;
    private Boolean mPaused = false;
    private int mRequestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int result = ContextCompat.checkSelfPermission(MainActivity.this,ReadPermission[0]);
        if(result != PackageManager.PERMISSION_GRANTED) {
//            throw new SecurityException("获取存储读取权限失败 !"); // this Exception will cause app exit !!
            //we request permission here.
            ActivityCompat.requestPermissions(MainActivity.this, ReadPermission, mRequestCode);
        }

        // use AssertFileDescriptor to access music file.
        try {
            mFd = getResources().getAssets().openFd(mMusicFIle);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mStart = (Button) findViewById(R.id.btnPlay);
        mStop = (Button) findViewById(R.id.btnStop);
        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnPlay:
                if(!mRunning || mPaused) {
                    if(!mPaused) {
                        preparePlay();
                    }
                    player.start();
                    mRunning = true;
                    mPaused = false;
                    mStart.setText("Playing");
                } else {
                    player.pause();
                    mStart.setText("Paused");
                    mPaused = true;
                }
                break;
            case R.id.btnStop:
                player.stop();
                player.release();
                player = null;
                mRunning = false;
                mPaused = false;
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == mRequestCode) {
            for (int i=0; i<ReadPermission.length; i++){
                String msg = String.format("%s : %d !", permissions[i], grantResults[i]);
                System.out.println(msg);

                if(grantResults[i] != 0) {
                    Toast.makeText(MainActivity.this, "请给与适当的权限来运行该程序 !", Toast.LENGTH_LONG).show();

                    // this finish() will end Current Activity UI.
                    finish();
//                    throw new SecurityException("没有权限运行!")
                }
            }
        }
    }

    public void preparePlay(){

        String path = Environment.getExternalStorageDirectory() + "/Music/test.mp3";

        if(player==null) {
            player = new MediaPlayer();
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                player.stop();
                player.release();
                player = null;
                mRunning = false;
                mPaused = false;
            }
        });

        try {
//            player.setDataSource("/sdcard/Music/test.mp3");
//            System.out.println(path + "eleven ======");
//            player.setDataSource("/data/test.mp3");
//            player.setDataSource(path);

            // use AssertFileDescriptor to setDataSource().
            player.setDataSource(mFd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
