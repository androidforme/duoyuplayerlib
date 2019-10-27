package com.wangduoyu.lib.duoyuplayerlib.controller;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wangduoyu.lib.duoyuplayerlib.R;
import com.wangduoyu.lib.duoyuplayerlib.constant.ConstantKeys;
import com.wangduoyu.lib.duoyuplayerlib.utils.DuoYuLog;
import com.wangduoyu.lib.duoyuplayerlib.utils.TimeUtils;
import com.wangduoyu.lib.duoyuplayerlib.utils.VideoPlayerUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DuoYuPlayerController extends AbsPlayerController implements View.OnClickListener {

    private ImageView mImage;
    private ImageView mCenterStart;
    private LinearLayout mTop;
    private ImageView mBack;
    private TextView mTitle;

    private LinearLayout mLlTopOther;
    private ImageView mIvDownload;
    private ImageView mIvAudio;
    private ImageView mIvShare;
    private ImageView mIvMenu;

    private LinearLayout mLlHorizontal;
    private ImageView mIvHorAudio;
    private ImageView mIvHorTv;
    private ImageView mBattery;
    private TextView mTime;

    private LinearLayout mBottom;
    private ImageView mRestartPause;
    private TextView mPosition;
    private TextView mDuration;
    private SeekBar mSeek;
    private TextView mClarity;
    private ImageView mFullScreen;
    private TextView mLength;
    private LinearLayout mLoading;
    private ProgressBar pbLoadingRing;
    private ProgressBar pbLoadingQq;
    private TextView mLoadText;
    private LinearLayout mChangePosition;
    private TextView mChangePositionCurrent;
    private ProgressBar mChangePositionProgress;
    private LinearLayout mChangeBrightness;
    private TextView mChangeBrightnessText;
    private ProgressBar mChangeBrightnessProgress;
    private LinearLayout mChangeVolume;
    private ProgressBar mChangeVolumeProgress;
    private TextView mChangeVolumeText;
    private LinearLayout mError;
    private TextView mTvError;
    private TextView mRetry;
    private LinearLayout mCompleted;
    private TextView mReplay;
    private TextView mShare;
    private FrameLayout mFlLock;
    private ImageView mIvLock;
    private LinearLayout mLine;
    private ProgressBar mPbPlayBar;

    private Timer mUpdateNetSpeedTimer;
    private TimerTask mUpdateNetSpeedTask;

    private boolean topBottomVisible;
    private CountDownTimer mDismissTopBottomCountDownTimer; //倒计时器

    /**
     * 这个是time时间不操作界面，则自动隐藏顶部和底部视图布局
     */
    private long time;

    public DuoYuPlayerController(@NonNull Context context) {
        super(context);
        init();
    }

    /**
     * 当播放器的播放模式发生变化时
     *
     * @param playMode 播放器的模式：
     */
    @Override
    public void onPlayModeChanged(int playMode) {
        switch (playMode) {
            //普通模式
            case ConstantKeys.PlayMode.MODE_NORMAL:
                //    mFlLock.setVisibility(View.GONE);
                mBack.setVisibility(View.VISIBLE);
                mFullScreen.setImageResource(R.drawable.icon_biggest);
                mFullScreen.setVisibility(View.VISIBLE);
                //    mClarity.setVisibility(View.GONE);
                //    setTopVisibility(mIsTopAndBottomVisibility);
                //    mLlHorizontal.setVisibility(View.GONE);
                //    unRegisterBatterReceiver();
                //    mIsLock = false;
//                if (mOnPlayerTypeListener!=null){
//                    mOnPlayerTypeListener.onNormal();
//                }
                break;
            //全屏模式
            case ConstantKeys.PlayMode.MODE_FULL_SCREEN:
                //    mFlLock.setVisibility(View.VISIBLE);
                mBack.setVisibility(View.VISIBLE);
                mFullScreen.setVisibility(View.VISIBLE);
                mFullScreen.setImageResource(R.drawable.icon_smallest);
//                if (clarities != null && clarities.size() > 1) {
//                    mClarity.setVisibility(View.VISIBLE);
//                }
                //     mLlTopOther.setVisibility(GONE);
//                if (mIsTopAndBottomVisibility){
//                    mLlHorizontal.setVisibility(View.VISIBLE);
//                    mIvHorTv.setVisibility(mIsTvIconVisibility?VISIBLE:GONE);
//                    mIvHorAudio.setVisibility(mIsAudioIconVisibility?VISIBLE:GONE);
//                }else {
//                    mLlHorizontal.setVisibility(View.GONE);
//                }
//                registerBatterReceiver();
//                if (mOnPlayerTypeListener!=null){
//                    mOnPlayerTypeListener.onFullScreen();
//                }
                break;
            //小窗口模式
            case ConstantKeys.PlayMode.MODE_TINY_WINDOW:
//                mFlLock.setVisibility(View.GONE);
//                mBack.setVisibility(View.VISIBLE);
//                mClarity.setVisibility(View.GONE);
//                mIsLock = false;
//                if (mOnPlayerTypeListener!=null){
//                    mOnPlayerTypeListener.onTinyWindow();
//                }
                break;
            default:
                break;
        }
    }

    /**
     * 初始化操作
     */
    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.dy_video_player, this, true);
        initFindViewById();
        initListener();
        // registerNetChangedReceiver();
    }

    private void initFindViewById() {
        mTop = findViewById(R.id.top);
        mLoading = findViewById(R.id.loading);
        mLoadText = mLoading.findViewById(R.id.load_text);
        mBack = mTop.findViewById(R.id.back);
        mTitle = mTop.findViewById(R.id.title);
        mTime = mTop.findViewById(R.id.time);

        mChangeVolume = findViewById(R.id.change_volume);
        mChangeVolumeProgress = mChangeVolume.findViewById(R.id.change_volume_progress);
        mChangeVolumeText = mChangeVolume.findViewById(R.id.change_volume_text);

        mChangePosition = findViewById(R.id.change_position);
        mChangePositionCurrent = findViewById(R.id.change_position_current);
        mChangePositionProgress = findViewById(R.id.change_position_progress);

        mChangeBrightness = findViewById(R.id.change_brightness);
        mChangeBrightnessText =  mChangeBrightness.findViewById(R.id.change_brightness_text);
        mChangeBrightnessProgress = mChangeBrightness.findViewById(R.id.change_brightness_progress);

        mBottom = findViewById(R.id.bottom);
        mRestartPause = mBottom.findViewById(R.id.restart_or_pause);
        mSeek = mBottom.findViewById(R.id.seek);
        mFullScreen = mBottom.findViewById(R.id.full_screen);
        mPosition = mBottom.findViewById(R.id.position);
        mDuration = mBottom.findViewById(R.id.duration);
        mLine = findViewById(R.id.line);
        mPbPlayBar = mLine.findViewById(R.id.pb_play_bar);
    }

    private void initListener() {
        //   mCenterStart.setOnClickListener(this);
        mBack.setOnClickListener(this);

        //   mIvDownload.setOnClickListener(this);
        //    mIvShare.setOnClickListener(this);
        //    mIvAudio.setOnClickListener(this);
        //    mIvMenu.setOnClickListener(this);

        //    mIvHorAudio.setOnClickListener(this);
        //     mIvHorTv.setOnClickListener(this);

        mRestartPause.setOnClickListener(this);
        mFullScreen.setOnClickListener(this);
        //    mClarity.setOnClickListener(this);
        //     mRetry.setOnClickListener(this);
        //     mReplay.setOnClickListener(this);
        //     mShare.setOnClickListener(this);
        //    mFlLock.setOnClickListener(this);
        mSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlayer.isBufferingPaused() || mPlayer.isPaused()) {
                    mPlayer.restart();
                }
                long position = (long) (mPlayer.getDuration() * seekBar.getProgress() / 100f);
                mPlayer.seekTo(position);
                startDismissTopBottomTimer();
            }
        });
        this.setOnClickListener(this);
    }

    @Override
    public void reset() {
        topBottomVisible = false;
        cancelUpdateProgressTimer();
        cancelDismissTopBottomTimer();
        mSeek.setProgress(0);
        mSeek.setSecondaryProgress(0);

        //    mCenterStart.setVisibility(View.VISIBLE);
        //    mImage.setVisibility(View.VISIBLE);

        mBottom.setVisibility(View.GONE);
        mFullScreen.setImageResource(R.drawable.icon_biggest);

        //    mLength.setVisibility(View.VISIBLE);

        mTop.setVisibility(View.VISIBLE);
        //    mBack.setVisibility(View.GONE);

        mLoading.setVisibility(View.GONE);
        //    mError.setVisibility(View.GONE);
        //    mCompleted.setVisibility(View.GONE);
    }

    /**
     * 注意：跟重置有区别
     * 控制器意外销毁，比如手动退出，意外崩溃等等
     */
    @Override
    public void destroy() {
        //当播放完成就解除广播
        //   unRegisterNetChangedReceiver();
        //    unRegisterBatterReceiver();
        //结束timer
        cancelUpdateProgressTimer();
        cancelUpdateNetSpeedTimer();
    }

    /**
     * 当播放状态发生改变时
     *
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
                statePaused();

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

    private void statePaused() {
        mLoading.setVisibility(View.GONE);
        //    mCenterStart.setVisibility(mIsCenterPlayerVisibility ? View.VISIBLE : View.GONE);
        mRestartPause.setImageResource(R.drawable.icon_play);
        cancelDismissTopBottomTimer();
        cancelUpdateNetSpeedTimer();
    }

    @Override
    protected void updateProgress() {
        //获取当前播放的位置，毫秒
        long position = mPlayer.getCurrentPosition();
        //获取办法给总时长，毫秒
        long duration = mPlayer.getDuration();
        //获取视频缓冲百分比
        int bufferPercentage = mPlayer.getBufferPercentage();
        mSeek.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
        mSeek.setProgress(progress);
        mPbPlayBar.setProgress(progress);
        mPosition.setText(TimeUtils.formatTime(position));
        mDuration.setText(TimeUtils.formatTime(duration));
        // 更新时间
        mTime.setText(new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));

    }

    @Override
    protected void showChangePosition(long duration, int newPositionProgress) {
        mChangePosition.setVisibility(View.VISIBLE);
        long newPosition = (long) (duration * newPositionProgress / 100f);
        mChangePositionCurrent.setText(VideoPlayerUtils.formatTime(newPosition));
        mChangePositionProgress.setProgress(newPositionProgress);
        mSeek.setProgress(newPositionProgress);
        mPbPlayBar.setProgress(newPositionProgress);
        mPosition.setText(VideoPlayerUtils.formatTime(newPosition));
    }

    /**
     * 隐藏视频播放位置
     */
    @Override
    protected void hideChangePosition() {
        //隐藏
        mChangePosition.setVisibility(View.GONE);
    }

    /**
     * 展示视频播放音量
     *
     * @param newVolumeProgress 新的音量进度，取值1到100。
     */
    @Override
    protected void showChangeVolume(int newVolumeProgress) {
        mChangeVolume.setVisibility(View.VISIBLE);
        mChangeVolumeText.setText(newVolumeProgress + "%");
        mChangeVolumeProgress.setProgress(newVolumeProgress);
    }

    /**
     * 隐藏视频播放音量
     */
    @Override
    protected void hideChangeVolume() {
        mChangeVolume.setVisibility(View.GONE);
    }

    /**
     * 展示视频播放亮度
     *
     * @param newBrightnessProgress 新的亮度进度，取值1到100。
     */
    @Override
    protected void showChangeBrightness(int newBrightnessProgress) {
        mChangeBrightness.setVisibility(View.VISIBLE);
        mChangeBrightnessText.setText(newBrightnessProgress + "%");
        mChangeBrightnessProgress.setProgress(newBrightnessProgress);
    }

    /**
     * 隐藏视频播放亮度
     */
    @Override
    protected void hideChangeBrightness() {
        mChangeBrightness.setVisibility(View.GONE);
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    private void stateBufferingPlaying() {
        //   mError.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        //   mCenterStart.setVisibility(View.GONE);
        mRestartPause.setImageResource(R.drawable.icon_stop);
        mLoadText.setText("正在准备...");
        startDismissTopBottomTimer();
        cancelUpdateNetSpeedTimer();
    }

    private void startPlaying() {
        mLoading.setVisibility(View.GONE);
        //   mCenterStart.setVisibility(View.GONE);
        mRestartPause.setImageResource(R.drawable.icon_stop);
        startDismissTopBottomTimer();
        cancelUpdateNetSpeedTimer();
    }


    /**
     * 播放准备中
     */
    private void startPreparing() {

        //   mImage.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        mLoadText.setText("正在准备...");
        //   mError.setVisibility(View.GONE);
        //    mCompleted.setVisibility(View.GONE);
        mTop.setVisibility(View.GONE);
        mBottom.setVisibility(View.GONE);
        //    mCenterStart.setVisibility(View.GONE);
        //    mLength.setVisibility(View.GONE);
        //开启缓冲时更新网络加载速度
        startUpdateNetSpeedTimer();
        startUpdateProgressTimer();
    }

    /**
     * 尽量不要在onClick中直接处理控件的隐藏、显示及各种UI逻辑。
     * UI相关的逻辑都尽量到{@link #onPlayStateChanged}和{@link #onPlayModeChanged}中处理.
     */
    @Override
    public void onClick(View v) {
        if (v == mRestartPause) {
            if (VideoPlayerUtils.isConnected(mContext)) {
                //重新播放或者暂停
                if (mPlayer.isPlaying() || mPlayer.isBufferingPlaying()) {
                    mPlayer.pause();
//                    if (mOnPlayOrPauseListener != null) {
//                        mOnPlayOrPauseListener.onPlayOrPauseClick(true);
//                    }
                } else if (mPlayer.isPaused() || mPlayer.isBufferingPaused()) {
                    mPlayer.restart();
//                    if (mOnPlayOrPauseListener != null) {
//                        mOnPlayOrPauseListener.onPlayOrPauseClick(false);
//                    }
                }
            } else {
                Toast.makeText(mContext, "请检测是否有网络", Toast.LENGTH_SHORT).show();
            }

        } else if (v == mBack) {
            //退出，执行返回逻辑
            //如果是全屏，则先退出全屏
            if (mPlayer.isFullScreen()) {
                mPlayer.exitFullScreen();
            } else if (mPlayer.isTinyWindow()) {
                //如果是小窗口，则退出小窗口
                // mPlayer.exitTinyWindow();
            } else {
                //如果两种情况都不是，执行逻辑交给使用者自己实现
//                if(mBackListener!=null){
//                    mBackListener.onBackClick();
//                }else {
//                    VideoLogUtil.d("返回键逻辑，如果是全屏，则先退出全屏；如果是小窗口，则退出小窗口；如果两种情况都不是，执行逻辑交给使用者自己实现");
//                }
            }
        } else if (v == mFullScreen) {
            //全屏模式，重置锁屏，设置为未选中状态
            if (mPlayer.isNormal() || mPlayer.isTinyWindow()) {
//                mFlLock.setVisibility(VISIBLE);
//                mIsLock = false;
//                mIvLock.setImageResource(R.drawable.player_unlock_btn);
                mPlayer.enterFullScreen();
            } else if (mPlayer.isFullScreen()) {
                //    mFlLock.setVisibility(GONE);
                mPlayer.exitFullScreen();
            }
        } else if (v == this) {
            if (mPlayer.isPlaying() || mPlayer.isPaused()
                    || mPlayer.isBufferingPlaying() || mPlayer.isBufferingPaused()) {
                setTopBottomVisible(!topBottomVisible);
            }
        }
    }


    /**
     * 更新网络加载速度
     */
    protected void updateNetSpeedProgress() {
        //获取网络加载速度
        long tcpSpeed = mPlayer.getTcpSpeed();
        //  DuoYuLog.i("获取网络加载速度++++++++"+tcpSpeed);
        if (tcpSpeed > 0) {
            int speed = (int) (tcpSpeed / 1024);
            //显示网速
            mLoading.setVisibility(View.VISIBLE);
            mLoadText.setText("网速" + speed + "kb");
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
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private void setTopBottomVisible(boolean visible) {
        mTop.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        mLine.setVisibility(visible ? View.GONE : View.VISIBLE);
        topBottomVisible = visible;
        if (visible) {
            if (!mPlayer.isPaused() && !mPlayer.isBufferingPaused()) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
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

    /**
     * 开启top、bottom自动消失的timer
     * 比如，视频常用功能，当用户6秒不操作后，自动隐藏头部和顶部
     */
    private void startDismissTopBottomTimer() {
        if (time == 0) {
            time = 6000;
        }
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(time, time) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }
}
