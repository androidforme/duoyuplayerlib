package com.wangduoyu.lib.duoyuplayerlib.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wangduoyu.lib.duoyuplayerlib.constant.ConstantKeys;
import com.wangduoyu.lib.duoyuplayerlib.interfaces.IPlayer;
import com.wangduoyu.lib.duoyuplayerlib.utils.DuoYuLog;
import com.wangduoyu.lib.duoyuplayerlib.utils.VideoPlayerUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 控制器抽象类
 */
public abstract class AbsPlayerController extends FrameLayout implements View.OnTouchListener {
    public Context mContext;
    protected IPlayer mPlayer;

    // 更新进度条是必须的
    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;

    private float mDownX;
    private float mDownY;

    /**
     * 是否需要改变播放的进度
     */
    private boolean mNeedChangePosition;
    /**
     * 是否需要改变播放的声音
     */
    private boolean mNeedChangeVolume;
    /**
     * 是否需要改变播放的亮度
     */
    private boolean mNeedChangeBrightness;

    private static final int THRESHOLD = 80;

    private long mGestureDownPosition;
    private float mGestureDownBrightness;
    private int mGestureDownVolume;
    private long mNewPosition;

    public AbsPlayerController(@NonNull Context context) {
        super(context);
        mContext = context;
        this.setOnTouchListener(this);
    }


    public void setVideoPlayer(IPlayer player) {
        mPlayer = player;
    }

    /**
     * 当播放器的播放模式发生变化，在此方法中更新不同模式下的控制器界面。
     *
     * @param playMode 播放器的模式：
     */
    public abstract void onPlayModeChanged(int playMode);


    /**
     * 重置控制器，将控制器恢复到初始状态。
     */
    public abstract void reset();

    /**
     * 控制器意外销毁，比如手动退出，意外崩溃等等
     */
    public abstract void destroy();

    public abstract void onPlayStateChanged(int mCurrentState);

    /**
     * 更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
     */
    protected abstract void updateProgress();

    /**
     * 手势左右滑动改变播放位置时，显示控制器中间的播放位置变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param duration            视频总时长ms
     * @param newPositionProgress 新的位置进度，取值0到100。
     */
    protected abstract void showChangePosition(long duration, int newPositionProgress);


    /**
     * 手势左右滑动改变播放位置后，手势up或者cancel时，隐藏控制器中间的播放位置变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract void hideChangePosition();


    /**
     * 手势在右侧上下滑动改变音量时，显示控制器中间的音量变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newVolumeProgress 新的音量进度，取值1到100。
     */
    protected abstract void showChangeVolume(int newVolumeProgress);

    /**
     * 手势在左侧上下滑动改变音量后，手势up或者cancel时，隐藏控制器中间的音量变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract void hideChangeVolume();

    /**
     * 手势在左侧上下滑动改变亮度时，显示控制器中间的亮度变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newBrightnessProgress 新的亮度进度，取值1到100。
     */
    protected abstract void showChangeBrightness(int newBrightnessProgress);

    /**
     * 手势在左侧上下滑动改变亮度后，手势up或者cancel时，隐藏控制器中间的亮度变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract void hideChangeBrightness();

    /**
     * 设置播放的视频的标题
     *
     * @param title 视频标题
     */
    public abstract void setTitle(String title);

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

    /**
     * 滑动处理调节声音和亮度的逻辑
     * @param v                         v
     * @param event                     event
     * @return                          是否自己处理滑动事件
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //不能用这个做判断，如果是小窗口播放状态，那么这个返回时false
        //boolean tinyWindow = mVideoPlayer.isTinyWindow();
        int playType = mPlayer.getPlayType();
        //如果是小窗口模式，则可以拖拽。其他情况则正常处理
        if(playType == ConstantKeys.PlayMode.MODE_FULL_SCREEN){
            //处理全屏播放时，滑动处理调节声音和亮度的逻辑
            return setOnTouch(v,event);
        }
        return false;
    }

    /**
     * 处理全屏播放时，滑动处理调节声音和亮度的逻辑
     * @param v                         v
     * @param event                     event
     * @return                          是否自己处理滑动事件
     */
    private boolean setOnTouch(View v, MotionEvent event) {
        // 只有全屏的时候才能拖动位置、亮度、声音
        if (!mPlayer.isFullScreen()) {
            return false;
        }
        // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
        if (mPlayer.isIdle() || mPlayer.isError() || mPlayer.isPreparing()
                || mPlayer.isPrepared() || mPlayer.isCompleted()) {
            //势左右滑动改变播放位置后，手势up或者cancel时，隐藏控制器中间的播放位置变化视图
            hideChangePosition();
            //手势在左侧上下滑动改变亮度后，手势up或者cancel时，隐藏控制器中间的亮度变化视图，
            hideChangeBrightness();
            //手势在左侧上下滑动改变音量后，手势up或者cancel时，隐藏控制器中间的音量变化视图，
            hideChangeVolume();
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //获取起点时的坐标
                mDownX = x;
                mDownY = y;
                mNeedChangePosition = false;
                mNeedChangeVolume = false;
                mNeedChangeBrightness = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //计算移动过程中的x，y轴的绝对值
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if (!mNeedChangePosition && !mNeedChangeVolume && !mNeedChangeBrightness) {
                    // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
                    if (absDeltaX >= THRESHOLD) {
                        cancelUpdateProgressTimer();
                        mNeedChangePosition = true;
                        mGestureDownPosition = mPlayer.getCurrentPosition();
                    } else if (absDeltaY >= THRESHOLD) {
                        if (mDownX < getWidth() * 0.5f) {
                            // 左侧改变亮度
                            mNeedChangeBrightness = true;
                            mGestureDownBrightness = VideoPlayerUtils.scanForActivity(mContext)
                                    .getWindow().getAttributes().screenBrightness;
                        } else {
                            // 右侧改变声音
                            mNeedChangeVolume = true;
                            mGestureDownVolume = mPlayer.getVolume();
                        }
                    }
                }
                if (mNeedChangePosition) {
                    long duration = mPlayer.getDuration();
                    long toPosition = (long) (mGestureDownPosition + duration * deltaX / getWidth());
                    mNewPosition = Math.max(0, Math.min(duration, toPosition));
                    int newPositionProgress = (int) (100f * mNewPosition / duration);
                    showChangePosition(duration, newPositionProgress);
                }
                if (mNeedChangeBrightness) {
                    deltaY = -deltaY;
                    float deltaBrightness = deltaY * 3 / getHeight();
                    float newBrightness = mGestureDownBrightness + deltaBrightness;
                    newBrightness = Math.max(0, Math.min(newBrightness, 1));
                    float newBrightnessPercentage = newBrightness;
                    WindowManager.LayoutParams params = VideoPlayerUtils.scanForActivity(mContext).getWindow().getAttributes();
                    params.screenBrightness = newBrightnessPercentage;
                    VideoPlayerUtils.scanForActivity(mContext).getWindow().setAttributes(params);
                    int newBrightnessProgress = (int) (100f * newBrightnessPercentage);
                    showChangeBrightness(newBrightnessProgress);
                }
                if (mNeedChangeVolume) {
                    deltaY = -deltaY;
                    int maxVolume = mPlayer.getMaxVolume();
                    int deltaVolume = (int) (maxVolume * deltaY * 3 / getHeight());
                    int newVolume = mGestureDownVolume + deltaVolume;
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume));
                    mPlayer.setVolume(newVolume);
                    int newVolumeProgress = (int) (100f * newVolume / maxVolume);
                    showChangeVolume(newVolumeProgress);
                }
                break;
            //滑动结束
            case MotionEvent.ACTION_CANCEL:
                //滑动手指抬起
            case MotionEvent.ACTION_UP:
                if (mNeedChangePosition) {
                    mPlayer.seekTo(mNewPosition);
                    hideChangePosition();
                    startUpdateProgressTimer();
                    return true;
                }
                if (mNeedChangeBrightness) {
                    hideChangeBrightness();
                    return true;
                }
                if (mNeedChangeVolume) {
                    hideChangeVolume();
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }
}
