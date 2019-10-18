package com.wangduoyu.lib.duoyuplayerlib.player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.wangduoyu.lib.duoyuplayerlib.constant.ConstantKeys;
import com.wangduoyu.lib.duoyuplayerlib.controller.AbsPlayerController;
import com.wangduoyu.lib.duoyuplayerlib.interfaces.IPlayer;
import com.wangduoyu.lib.duoyuplayerlib.manager.DuoYuPlayerManager;
import com.wangduoyu.lib.duoyuplayerlib.utils.DuoYuLog;
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

    @Override
    public void release() {

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
