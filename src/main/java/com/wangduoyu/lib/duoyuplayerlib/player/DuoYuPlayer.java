package com.wangduoyu.lib.duoyuplayerlib.player;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.wangduoyu.lib.duoyuplayerlib.constant.ConstantKeys;
import com.wangduoyu.lib.duoyuplayerlib.controller.AbsPlayerController;
import com.wangduoyu.lib.duoyuplayerlib.interfaces.IPlayer;
import com.wangduoyu.lib.duoyuplayerlib.manager.DuoYuPlayerManager;
import com.wangduoyu.lib.duoyuplayerlib.utils.DuoYuLog;
import com.wangduoyu.lib.duoyuplayerlib.utils.VideoPlayerUtils;
import com.wangduoyu.lib.duoyuplayerlib.view.DuoYuTextureView;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class DuoYuPlayer extends FrameLayout implements IPlayer {
    private final static String TAG = "DuoYuPlayer";

    private Context mContext;
    private FrameLayout mContainer; //容器

    private String mUrl;
    private Map<String, String> mHeaders;
    private int mCurrentState = ConstantKeys.CurrentState.STATE_IDLE;
    private int mPlayerType = ConstantKeys.IjkPlayerType.TYPE_IJK;  // 播放类型

    private AudioManager mAudioManager;
    private IMediaPlayer mMediaPlayer;

    private DuoYuTextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private int mBufferPercentage;

    private AbsPlayerController mController;

    /**
     * 播放模式，普通模式，小窗口模式，正常模式等等
     * 存在局限性：比如小窗口下的正在播放模式，那么mCurrentMode就是STATE_PLAYING，而不是MODE_TINY_WINDOW并存
     **/
    private int mCurrentMode = ConstantKeys.PlayMode.MODE_NORMAL;

    public DuoYuPlayer(Context context) {
        this(context, null);
    }

    public DuoYuPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
    }

    @Override
    public void setUp(String url, Map<String, String> headers) {
        DuoYuLog.d(" url = " + url);
        mUrl = url;
        mHeaders = headers;
    }

    public void setController(AbsPlayerController controller) {
        mContainer.removeView(mController);
        mController = controller;
        mController.reset();
        mController.setVideoPlayer(this);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mController, params);
    }

    /**
     * 开始播放
     */
    @Override
    public void start() {
        DuoYuLog.d();
        if (mCurrentState == ConstantKeys.CurrentState.STATE_IDLE) {
            DuoYuPlayerManager.getInstance().setCurrentPlayer(this);
            initAudioManager();
            initMediaPlayer();
            initTextureView();
            addTextureView();
        } else {
            DuoYuLog.d(" 只有在mCurrentState == STATE_IDLE时才能调用start方法.");
        }
    }


    private void initAudioManager() {
        DuoYuLog.d();
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    private void initMediaPlayer() {
        DuoYuLog.d();
        if (mMediaPlayer == null) {
            switch (mPlayerType) {
                //AndroidMediaPlayer和IjkMediaPlayer都是实现AbstractMediaPlayer
                //MediaPlayer
                case ConstantKeys.IjkPlayerType.TYPE_NATIVE:
                    mMediaPlayer = new AndroidMediaPlayer();
                    break;
                //IjkMediaPlayer    基于Ijk
                case ConstantKeys.IjkPlayerType.TYPE_IJK:
                default:
                    mMediaPlayer = new IjkMediaPlayer();
                    //todo 需要去细化ijk播放器
                    //  createIjkMediaPlayer();
                    break;
            }
            //设置音频流类型
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private void initTextureView() {
        DuoYuLog.d();
        if (mTextureView == null) {
            mTextureView = new DuoYuTextureView(mContext);
            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    DuoYuLog.d(" width = " + width + "; height = " + height);
                    if (mSurfaceTexture == null) {
                        mSurfaceTexture = surface;
                        openMediaPlayer();
                    } else {
                        mTextureView.setSurfaceTexture(mSurfaceTexture);
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    DuoYuLog.d(" width = " + width + "; height = " + height);

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    DuoYuLog.d(" surface = " + surface);
                    return mSurfaceTexture == null;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    //Log 太多
                    //  DuoYuLog.d(" surface = " + surface);
                }
            });
        }
    }

    private void openMediaPlayer() {
        //todo 先简单设置一下
        DuoYuLog.d();
        // 屏幕常亮
        mContainer.setKeepScreenOn(true);
        // 设置监听
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.setOnInfoListener(mOnInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        // 设置dataSource
        try {
            mMediaPlayer.setDataSource(mContext.getApplicationContext(), Uri.parse(mUrl), mHeaders);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.prepareAsync();
            mCurrentState = ConstantKeys.CurrentState.STATE_PREPARING;
            mController.onPlayStateChanged(mCurrentState);
            DuoYuLog.d("STATE_PREPARING");
        } catch (IOException e) {
            e.printStackTrace();
            DuoYuLog.e("打开播放器发生错误", e);
        }
    }


    private void addTextureView() {
        DuoYuLog.d();
        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mContainer.addView(mTextureView, 0, params);
    }

    /**
     * 设置播放器类型
     *
     * @param playerType IjkPlayer or MediaPlayer.
     */
    public void setPlayerType(@ConstantKeys.PlayerType int playerType) {
        mPlayerType = playerType;
    }

    @Override
    public void start(long position) {

    }

    @Override
    public void restart() {
        if (mCurrentState == ConstantKeys.CurrentState.STATE_PAUSED) {
            //如果是暂停状态，那么则继续播放
            mMediaPlayer.start();
            mCurrentState = ConstantKeys.CurrentState.STATE_PLAYING;
            mController.onPlayStateChanged(mCurrentState);
        } else if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED) {
            //如果是缓存暂停状态，那么则继续播放
            mMediaPlayer.start();
            mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING;
            mController.onPlayStateChanged(mCurrentState);
        } else if (mCurrentState == ConstantKeys.CurrentState.STATE_COMPLETED
                || mCurrentState == ConstantKeys.CurrentState.STATE_ERROR) {
            //如果是完成播放或者播放错误，则重新播放
            mMediaPlayer.reset();
            openMediaPlayer();
        } else {
            DuoYuLog.d("VideoPlayer在mCurrentState == " + mCurrentState + "时不能调用restart()方法.");
        }
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        if (mCurrentState == ConstantKeys.CurrentState.STATE_PLAYING) {
            //如果是播放状态，那么则暂停播放
            mMediaPlayer.pause();
            mCurrentState = ConstantKeys.CurrentState.STATE_PAUSED;
            mController.onPlayStateChanged(mCurrentState);
        } else if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING) {
            //如果是正在缓冲状态，那么则暂停暂停缓冲
            mMediaPlayer.pause();
            mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED;
            mController.onPlayStateChanged(mCurrentState);
        }
    }

    @Override
    public void setSpeed(float speed) {

    }

    @Override
    public long getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public long getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    /**
     * 进入全屏模式
     * 全屏，将mContainer(内部包含mTextureView和mController)从当前容器中移除，并添加到android.R.content中.
     * 切换横屏时需要在manifest的activity标签下添加android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期
     */
    @Override
    public void enterFullScreen() {
        if (mCurrentMode == ConstantKeys.PlayMode.MODE_FULL_SCREEN){
            return;
        }
        // 隐藏ActionBar、状态栏，并横屏
        ImmersionBar.with(VideoPlayerUtils.scanForActivity(mContext)).hideBar(BarHide.FLAG_HIDE_BAR).init();
        //设置更改此页面的所需方向。如果页面当前位于前台或以其他方式影响方向
        //则屏幕将立即更改(可能导致重新启动该页面)。否则，这将在下一次页面可见时使用。
        VideoPlayerUtils.scanForActivity(mContext).setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //找到contentView
        ViewGroup contentView = VideoPlayerUtils.scanForActivity(mContext)
                .findViewById(android.R.id.content);
        if (mCurrentMode == ConstantKeys.PlayMode.MODE_TINY_WINDOW) {
            contentView.removeView(mContainer);
        } else {
            this.removeView(mContainer);
        }
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(mContainer, params);
        mCurrentMode = ConstantKeys.PlayMode.MODE_FULL_SCREEN;
        mController.onPlayModeChanged(mCurrentMode);
        DuoYuLog.d("MODE_FULL_SCREEN");
    }


    /**
     * 退出全屏模式
     * 退出全屏，移除mTextureView和mController，并添加到非全屏的容器中。
     * 切换竖屏时需要在manifest的activity标签下添加
     * android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期.
     *
     * @return true退出全屏.
     */
    @Override
    public boolean exitFullScreen() {
        if (mCurrentMode == ConstantKeys.PlayMode.MODE_FULL_SCREEN) {
            ImmersionBar.with(VideoPlayerUtils.scanForActivity(mContext)).hideBar(BarHide.FLAG_SHOW_BAR).init();
            VideoPlayerUtils.scanForActivity(mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ViewGroup contentView = VideoPlayerUtils.scanForActivity(mContext).findViewById(android.R.id.content);
            //将视图移除
            contentView.removeView(mContainer);
            //重新添加到当前视图
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mContainer, params);
            mCurrentMode = ConstantKeys.PlayMode.MODE_NORMAL;
            mController.onPlayModeChanged(mCurrentMode);
            DuoYuLog.d("MODE_NORMAL");
            this.setOnKeyListener(null);
            return true;
        }
        return false;
    }


    /**
     * 获取播放速度
     *
     * @return 速度
     */
    @Override
    public long getTcpSpeed() {
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            return ((IjkMediaPlayer) mMediaPlayer).getTcpSpeed();
        }
        return 0;
    }

    /**
     * 设置播放位置
     *
     * @param pos 播放位置
     */
    @Override
    public void seekTo(long pos) {
        if (pos < 0) {
            pos = 0;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
        }
    }

    /**
     * 判断是否开始播放
     *
     * @return true表示播放未开始
     */
    @Override
    public boolean isIdle() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_IDLE;
    }


    /**
     * 判断视频是否播放准备中
     *
     * @return true表示播放准备中
     */
    @Override
    public boolean isPreparing() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_PREPARING;
    }


    /**
     * 判断视频是否准备就绪
     *
     * @return true表示播放准备就绪
     */
    @Override
    public boolean isPrepared() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_PREPARED;
    }


    /**
     * 判断视频是否正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     *
     * @return true表示正在缓冲
     */
    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING;
    }


    /**
     * 判断是否是否缓冲暂停
     *
     * @return true表示缓冲暂停
     */
    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED;
    }


    /**
     * 判断视频是否正在播放
     *
     * @return true表示正在播放
     */
    @Override
    public boolean isPlaying() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_PLAYING;
    }


    /**
     * 判断视频是否暂停播放
     *
     * @return true表示暂停播放
     */
    @Override
    public boolean isPaused() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_PAUSED;
    }


    /**
     * 判断视频是否播放错误
     *
     * @return true表示播放错误
     */
    @Override
    public boolean isError() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_ERROR;
    }


    /**
     * 判断视频是否播放完成
     *
     * @return true表示播放完成
     */
    @Override
    public boolean isCompleted() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_COMPLETED;
    }

    /**
     * 判断视频是否播放全屏
     * @return                      true表示播放全屏
     */
    @Override
    public boolean isFullScreen() {
        return mCurrentMode == ConstantKeys.PlayMode.MODE_FULL_SCREEN;
    }


    /**
     * 判断视频是否播放小窗口
     * @return                      true表示播放小窗口
     */
    @Override
    public boolean isTinyWindow() {
        return mCurrentMode == ConstantKeys.PlayMode.MODE_TINY_WINDOW;
    }


    /**
     * 判断视频是否正常播放
     * @return                      true表示正常播放
     */
    @Override
    public boolean isNormal() {
        return mCurrentMode == ConstantKeys.PlayMode.MODE_NORMAL;
    }

    @Override
    public void releasePlayer() {
        if (mAudioManager != null) {
            //放弃音频焦点。使以前的焦点所有者(如果有的话)接收焦点。
            mAudioManager.abandonAudioFocus(null);
            //置空
            mAudioManager = null;
        }
        if (mMediaPlayer != null) {
            //释放视频焦点
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mContainer != null) {
            //从视图中移除TextureView
            mContainer.removeView(mTextureView);
        }
        if (mSurface != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mSurface.release();
            }
            mSurface = null;
        }
        //如果SurfaceTexture不为null，则释放
        if (mSurfaceTexture != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mSurfaceTexture.release();
            }
            mSurfaceTexture = null;
        }
        mCurrentState = ConstantKeys.CurrentState.STATE_IDLE;
    }

    /**
     * 释放，内部的播放器被释放掉，同时如果在全屏、小窗口模式下都会退出
     * 逻辑
     * 1.先保存播放位置
     * 2.退出全屏或小窗口，回复播放模式为正常模式
     * 3.释放播放器
     * 4.恢复控制器
     * 5.gc回收
     */
    @Override
    public void release() {
        // 保存播放位置，当正在播放时，缓冲时，缓冲暂停时，暂停时 TODO
        if (isPlaying() || isBufferingPlaying() || isBufferingPaused() || isPaused()) {
            //  VideoPlayerUtils.savePlayPosition(mContext, mUrl, getCurrentPosition());
        } else if (isCompleted()) {
            //如果播放完成，则保存播放位置为0，也就是初始位置 TODO
            //  VideoPlayerUtils.savePlayPosition(mContext, mUrl, 0);
        }
        // 退出全屏或小窗口 TODO
//        if (isFullScreen()) {
//            exitFullScreen();
//        }
//        if (isTinyWindow()) {
//            exitTinyWindow();
//        }
//        mCurrentMode = ConstantKeys.PlayMode.MODE_NORMAL;

        // 释放播放器
        releasePlayer();

        // 恢复控制器
        if (mController != null) {
            mController.reset();
        }
        // gc回收
        Runtime.getRuntime().gc();
    }


    private IMediaPlayer.OnPreparedListener mOnPreparedListener
            = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            DuoYuLog.d();
            mCurrentState = ConstantKeys.CurrentState.STATE_PREPARED;
            mController.onPlayStateChanged(mCurrentState);
            mp.start();
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener
            = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            DuoYuLog.d(" width：" + width + "， height：" + height);
            mTextureView.adaptVideoSize(width, height);

        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener
            = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            DuoYuLog.d();
            mCurrentState = ConstantKeys.CurrentState.STATE_COMPLETED;
            mController.onPlayStateChanged(mCurrentState);
            // 清除屏幕常亮
            mContainer.setKeepScreenOn(false);
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener
            = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            DuoYuLog.d(" what：" + what + ", extra: " + extra);
            // 直播流播放时去调用mediaPlayer.getDuration会导致-38和-2147483648错误，忽略该错误
            if (what != -38 && what != -2147483648 && extra != -38 && extra != -2147483648) {
                mCurrentState = ConstantKeys.CurrentState.STATE_ERROR;
                mController.onPlayStateChanged(mCurrentState);
            }
            return true;
        }
    };

    private IMediaPlayer.OnInfoListener mOnInfoListener
            = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            DuoYuLog.d(" mp：" + mp + " what：" + what + ", extra: " + extra);
            if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // 播放器开始渲染
                mCurrentState = ConstantKeys.CurrentState.STATE_PLAYING;
                mController.onPlayStateChanged(mCurrentState);
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                // MediaPlayer暂时不播放，以缓冲更多的数据
                if (mCurrentState == ConstantKeys.CurrentState.STATE_PAUSED || mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED;
                } else {
                    mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING;
                }
                mController.onPlayStateChanged(mCurrentState);
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                // 填充缓冲区后，MediaPlayer恢复播放/暂停
                if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_PLAYING;
                    mController.onPlayStateChanged(mCurrentState);
                }
                if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_PAUSED;
                    mController.onPlayStateChanged(mCurrentState);
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                // 视频旋转了extra度，需要恢复
                if (mTextureView != null) {
                    mTextureView.setRotation(extra);
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                DuoYuLog.d("视频不能seekTo，为直播视频");
            } else {
                DuoYuLog.d("onInfo ——> what：" + what);
            }
            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener
            = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            DuoYuLog.d(" percent = " + percent);
            mBufferPercentage = percent;
        }
    };


}
