package com.wangduoyu.lib.duoyuplayerlib.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wangduoyu.lib.duoyuplayerlib.interfaces.IPlayer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 控制器抽象类
 */
public abstract class AbsPlayerController extends FrameLayout {
    public Context mContext;
    protected IPlayer mPlayer;

    // 更新进度条是必须的
    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;

    public AbsPlayerController(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public void setVideoPlayer(IPlayer player) {
        mPlayer = player;
    }


    /**
     * 重置控制器，将控制器恢复到初始状态。
     */
    public abstract void reset();

    public abstract void onPlayStateChanged(int mCurrentState);

    /**
     * 更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
     */
    protected abstract void updateProgress();

    /**
     * 开启更新进度的计时器。
     */
    protected void startUpdateProgressTimer() {
        cancelUpdateProgressTimer();
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = new Timer();
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = new TimerTask() {
                @Override
                public void run() {
                    //在子线程中更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
                    AbsPlayerController.this.post(new Runnable() {
                        @Override
                        public void run() {
                            updateProgress();
                        }
                    });
                }
            };
        }
        mUpdateProgressTimer.schedule(mUpdateProgressTimerTask, 0, 1000);
    }

    /**
     * 取消更新进度的计时器。
     */
    protected void cancelUpdateProgressTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask.cancel();
            mUpdateProgressTimerTask = null;
        }
    }
}
