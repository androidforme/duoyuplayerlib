package com.wangduoyu.lib.duoyuplayerlib.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wangduoyu.lib.duoyuplayerlib.R;
import com.wangduoyu.lib.duoyuplayerlib.constant.ConstantKeys;
import com.wangduoyu.lib.duoyuplayerlib.utils.DuoYuLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DuoYuPlayerController extends AbsPlayerController {

    private LinearLayout mLoading;
    private TextView mLoadText;

    private LinearLayout mBottom;
    private ProgressBar mPbPlayBar;

    private Timer mUpdateNetSpeedTimer;
    private TimerTask mUpdateNetSpeedTask;

    public DuoYuPlayerController(@NonNull Context context) {
        super(context);
        init();
    }

    /**
     * 初始化操作
     */
    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.dy_video_player, this, true);
        initFindViewById();
        //initListener();
       // registerNetChangedReceiver();
    }

    private void initFindViewById() {
        mLoading = findViewById(R.id.loading);
        mLoadText = mLoading.findViewById(R.id.load_text);

        mBottom = findViewById(R.id.bottom);
        mPbPlayBar = findViewById(R.id.pb_play_bar);
    }

    @Override
    public void reset() {

    }

    /**
     * 当播放状态发生改变时
     * @param playState 播放状态：
     */
    @Override
    public void onPlayStateChanged(int playState) {
        DuoYuLog.d(" playState = " + playState);
        switch (playState) {
            case ConstantKeys.CurrentState.STATE_IDLE:
                break;
            //播放准备中
            case ConstantKeys.CurrentState.STATE_PREPARING:
                startPreparing();
                break;
            //播放准备就绪
            case ConstantKeys.CurrentState.STATE_PREPARED:
                 startUpdateProgressTimer();
                //取消缓冲时更新网络加载速度
                cancelUpdateNetSpeedTimer();
                break;
            //正在播放
            case ConstantKeys.CurrentState.STATE_PLAYING:
                startPlaying();
                break;
            //暂停播放
            case ConstantKeys.CurrentState.STATE_PAUSED:
//                mLoading.setVisibility(View.GONE);
//                mCenterStart.setVisibility(mIsCenterPlayerVisibility?View.VISIBLE:View.GONE);
//                mRestartPause.setImageResource(R.drawable.ic_player_start);
//                cancelDismissTopBottomTimer();
//                cancelUpdateNetSpeedTimer();
                break;
            //正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
            case ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING:
                stateBufferingPlaying();
                break;
            //正在缓冲
            case ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED:
//                mLoading.setVisibility(View.VISIBLE);
//                mRestartPause.setImageResource(R.drawable.ic_player_start);
//                mLoadText.setText("正在准备...");
//                cancelDismissTopBottomTimer();
//                //开启缓冲时更新网络加载速度
//                startUpdateNetSpeedTimer();
                break;
            //播放错误
            case ConstantKeys.CurrentState.STATE_ERROR:
             //   stateError();
                break;
            //播放完成
            case ConstantKeys.CurrentState.STATE_COMPLETED:
             //   stateCompleted();
                break;
            default:
                break;
        }
    }

    @Override
    protected void updateProgress() {
        //获取当前播放的位置，毫秒
        long position = mPlayer.getCurrentPosition();
        //获取办法给总时长，毫秒
        long duration = mPlayer.getDuration();
        //获取视频缓冲百分比
        int bufferPercentage = mPlayer.getBufferPercentage();
      //  mSeek.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
     //   mSeek.setProgress(progress);
        mPbPlayBar.setProgress(progress);
   //     mPosition.setText(VideoPlayerUtils.formatTime(position));
   //     mDuration.setText(VideoPlayerUtils.formatTime(duration));
        // 更新时间
   //     mTime.setText(new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));

    }

    private void stateBufferingPlaying() {
     //   mError.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
     //   mCenterStart.setVisibility(View.GONE);
     //   mRestartPause.setImageResource(R.drawable.ic_player_pause);
        mLoadText.setText("正在准备...");
     //   startDismissTopBottomTimer();
         cancelUpdateNetSpeedTimer();
    }

    private void startPlaying() {
        mLoading.setVisibility(View.GONE);
//        mCenterStart.setVisibility(View.GONE);
//        mRestartPause.setImageResource(R.drawable.ic_player_pause);
//        startDismissTopBottomTimer();
//        cancelUpdateNetSpeedTimer();
    }


    /**
     * 播放准备中
     */
    private void startPreparing() {

        mLoading.setVisibility(View.VISIBLE);
        mLoadText.setText("正在准备...");

        mBottom.setVisibility(View.GONE);
        //开启缓冲时更新网络加载速度
        startUpdateNetSpeedTimer();
        startUpdateProgressTimer();
    }


    /**
     *  更新网络加载速度
     */
    protected void updateNetSpeedProgress() {
        //获取网络加载速度
        long tcpSpeed = mPlayer.getTcpSpeed();
      //  DuoYuLog.i("获取网络加载速度++++++++"+tcpSpeed);
        if (tcpSpeed>0){
            int speed = (int) (tcpSpeed/1024);
            //显示网速
            mLoading.setVisibility(View.VISIBLE);
            mLoadText.setText("网速"+speed+"kb");
        }
    }

    /**
     * 当正在缓冲或者播放准备中状态时，开启缓冲时更新网络加载速度
     */
    protected void startUpdateNetSpeedTimer() {
        cancelUpdateNetSpeedTimer();
        if (mUpdateNetSpeedTimer == null) {
            mUpdateNetSpeedTimer = new Timer();
        }
        if (mUpdateNetSpeedTask == null) {
            mUpdateNetSpeedTask = new TimerTask() {
                @Override
                public void run() {
                    //在子线程中更新进度，包括更新网络加载速度
                    post(new Runnable() {
                        @Override
                        public void run() {
                            updateNetSpeedProgress();
                        }
                    });
                }
            };
        }
        mUpdateNetSpeedTimer.schedule(mUpdateNetSpeedTask, 0, 500);
    }

    /**
     * 取消缓冲时更新网络加载速度
     */
    protected void cancelUpdateNetSpeedTimer() {
        if (mUpdateNetSpeedTimer != null) {
            mUpdateNetSpeedTimer.cancel();
            mUpdateNetSpeedTimer = null;
        }
        if (mUpdateNetSpeedTask != null) {
            mUpdateNetSpeedTask.cancel();
            mUpdateNetSpeedTask = null;
        }
    }
}
