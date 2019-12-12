package com.mole.videoplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

import static android.view.SurfaceHolder.Callback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int FILE_SELECT_CODE = 1;
    private final String TAG = "NYDBG";
    private Button play, pause, stop, pick;
    private Boolean isPrepared = false;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private int position;
    private Context mContext;
    String path = Environment.getExternalStorageDirectory() + File.separator + "flutter.mp4";
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
//        getWindow().setAttributes(params);
        mContext = this;
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pasue);
        stop = findViewById(R.id.stop);
        pick = findViewById(R.id.pick);
        FileUtils.verifyStoragePermissions(this);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        play.setOnClickListener(this);

        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        pick.setOnClickListener(this);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(MainActivity.this, "视频播放完毕", Toast.LENGTH_SHORT).show();
            }
        });

        surfaceHolder.addCallback(new Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("NYDBG", "surfaceCreated position " + position);
                if (!"".equals(uri) && !mediaPlayer.isPlaying()) {
                    play(position);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("NYDBG", "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("NYDBG", "surfaceDestroyed");
                if (mediaPlayer.isPlaying()) {
                    position = mediaPlayer.getCurrentPosition();
                    Log.d(TAG, "当前播放时间：" + position);
                    mediaPlayer.stop();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    path = FileUtils.getPathByUri(getApplicationContext(), uri);
                    Log.d(TAG, "File Path: " + path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void play(int position) {
        Log.d(TAG, "续播时间：" + position);
        try {
            mediaPlayer.reset();
//            mediaPlayer.setDataSource(path);//API<29
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(position, MediaPlayer.SEEK_CLOSEST);
            mediaPlayer.setDisplay(surfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    public void doPrepare(Uri uri) {
        Log.d("NYDBG", "doPrepare");
        try {
            mediaPlayer.reset();
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setDataSource(mContext,uri);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                Log.d("NYDBG", "onClick isPrepared " + isPrepared);
                if (!isPrepared) {
                    doPrepare(uri);
                    isPrepared = true;
                    position = 0;
                } else {
                    play(position);
                }
                break;
            case R.id.pasue:
                if (mediaPlayer.isPlaying()) {
                    position = mediaPlayer.getCurrentPosition();
                    Log.d(TAG, "当前播放时间：" + position);
                    mediaPlayer.pause();
                }
                break;
            case R.id.stop:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    isPrepared = true;
                    position = 0;
                    Log.d("NYDBG", "stop");
                }
                break;
            case R.id.pick:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/mp4");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}