package com.wangduoyu.lib.duoyuplayerlib.player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.wangduoyu.lib.duoyuplayerlib.constant.ConstantKeys;
import com.wangduoyu.lib.duoyuplayerlib.controller.AbsPlayerController;
import com.wangduoyu.lib.duoyuplayerlib.interfaces.IPlayer;
import com.wangduoyu.lib.duoyuplayerlib.interfaces.listener.OnSurfaceListener;
import com.wangduoyu.lib.duoyuplayerlib.utils.DyLog;
import com.wangduoyu.lib.duoyuplayerlib.view.VideoTextureView;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * 播放器 处理视频播放器各种状态和初始设置
 * revise: 注意：在对应的播放Activity页面，清单文件中一定要添加
 * android:configChanges="orientation|keyboardHidden|screenSize"
 * android:screenOrientation="portrait"
 */
public class DyPlayer extends FrameLayout implements IPlayer {
    private Context mContext;

    private FrameLayout mContainer;
    private String mUrl; // 电源路径
    private Map<String, String> mHeaders; //请求头

    /**
     * 播放状态，错误，开始播放，暂停播放，缓存中等等状态
     **/
    private int mCurrentState = ConstantKeys.CurrentState.STATE_IDLE;

    private IMediaPlayer mMediaPlayer;
    private VideoTextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private AbsPlayerController mController; //播放控制器




    public DyPlayer(@NonNull Context context) {
        this(context, null);
    }

    public DyPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DyPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mContainer = new FrameLayout(mContext);
        //设置背景颜色，目前设置为纯黑色
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
    }

    @Override
    public void setUp(String url, Map<String, String> headers) {
        if (url == null || url.length() == 0) {
            DyLog.e("url is null", new Throwable("url is null!!!"));
        }
        mUrl = url;
        mHeaders = headers;
    }

    @Override
    public void start() {
        if (mCurrentState == ConstantKeys.CurrentState.STATE_IDLE) {
        //    VideoPlayerManager.instance().setCurrentVideoPlayer(this);
        //    initAudioManager();
            createIjkMediaPlayer();
            //设置音频流类型
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                initTextureView();
            }else {
                DyLog.e("API < 14 ,不支持.");
            }
        } else {
            DyLog.d("DyPlayer只有在mCurrentState == STATE_IDLE时才能调用start方法.");
        }

    }


    private void createIjkMediaPlayer() {
        //创建IjkMediaPlayer对象
        mMediaPlayer = new IjkMediaPlayer();
        int player = IjkMediaPlayer.OPT_CATEGORY_PLAYER;
        int codec = IjkMediaPlayer.OPT_CATEGORY_CODEC;
        int format = IjkMediaPlayer.OPT_CATEGORY_FORMAT;

        //设置ijkPlayer播放器的硬件解码相关参数
        //设置播放前的最大探测时间
        ((IjkMediaPlayer)mMediaPlayer).setOption(format, "analyzemaxduration", 100L);
        //设置播放前的探测时间 1,达到首屏秒开效果
        ((IjkMediaPlayer)mMediaPlayer).setOption(format, "analyzeduration", 1L);
        //播放前的探测Size，默认是1M, 改小一点会出画面更快
        ((IjkMediaPlayer)mMediaPlayer).setOption(format, "probesize", 10240L);
        //设置是否开启变调isModifyTone?0:1
        ((IjkMediaPlayer)mMediaPlayer).setOption(player,"soundtouch",0);
        //每处理一个packet之后刷新io上下文
        ((IjkMediaPlayer)mMediaPlayer).setOption(format, "flush_packets", 1L);
        //是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "packet-buffering", 0L);
        //播放重连次数
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "reconnect", 5);
        //最大缓冲大小,单位kb
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "max-buffer-size", 10240L);
        //跳帧处理,放CPU处理较慢时，进行跳帧处理，保证播放流程，画面和声音同步
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "framedrop", 1L);
        //最大fps
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "max-fps", 30L);
        //SeekTo设置优化
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "enable-accurate-seek", 1L);
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "opensles", 0);
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "framedrop", 1);
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "start-on-prepared", 0);
        ((IjkMediaPlayer)mMediaPlayer).setOption(format, "http-detect-range-support", 0);
        //设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
        ((IjkMediaPlayer)mMediaPlayer).setOption(codec, "skip_loop_filter", 48);

        //jkPlayer支持硬解码和软解码。
        //软解码时不会旋转视频角度这时需要你通过onInfo的what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED去获取角度，自己旋转画面。
        //或者开启硬解硬解码，不过硬解码容易造成黑屏无声（硬件兼容问题），下面是设置硬解码相关的代码
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "mediacodec", 0);
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "mediacodec-auto-rotate", 1);
        ((IjkMediaPlayer)mMediaPlayer).setOption(player, "mediacodec-handle-resolution-change", 1);
    }


        /**
         * 初始化TextureView
         * 这个主要是用作视频的
         */
        @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        private void initTextureView() {
            if (mTextureView == null) {
                mTextureView = new VideoTextureView(mContext);
                mTextureView.setOnSurfaceListener(new OnSurfaceListener() {
                    @Override
                    public void onSurfaceAvailable(SurfaceTexture surface) {
                        if (mSurfaceTexture == null) {
                            mSurfaceTexture = surface;
                            openMediaPlayer();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mTextureView.setSurfaceTexture(mSurfaceTexture);
                            }
                        }
                    }

                    @Override
                    public void onSurfaceSizeChanged(SurfaceTexture surface, int width, int height) {

                    }

                    @Override
                    public boolean onSurfaceDestroyed(SurfaceTexture surface) {
                        return mSurfaceTexture == null;
                    }

                    @Override
                    public void onSurfaceUpdated(SurfaceTexture surface) {

                    }
                });
            }
            mTextureView.addTextureView(mContainer,mTextureView);
        }

    @Override
    public void start(long position) {

    }


    /**
     * 打开MediaPlayer播放器
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void openMediaPlayer() {
        // 屏幕常亮
        mContainer.setKeepScreenOn(true);
        // 设置监听，可以查看ijk中的IMediaPlayer源码监听事件
        // 设置准备视频播放监听事件
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        // 设置视频播放完成监听事件
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        // 设置视频缓冲更新监听事件
        mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        // 设置视频seek完成监听事件
        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        // 设置视频大小更改监听器
        mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        // 设置视频错误监听器
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        // 设置视频信息监听器
        mMediaPlayer.setOnInfoListener(mOnInfoListener);
        // 设置时间文本监听器
        mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
        // 设置dataSource
        if(mUrl==null || mUrl.length()==0){
            Toast.makeText(mContext,"视频链接不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        Uri path = Uri.parse(mUrl);
        try {
            mMediaPlayer.setDataSource(mContext.getApplicationContext(), path, mHeaders);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            // 设置surface
            mMediaPlayer.setSurface(mSurface);
            // 设置播放时常亮
            mMediaPlayer.setScreenOnWhilePlaying(true);
            // 开始加载
            mMediaPlayer.prepareAsync();
            // 播放准备中
            mCurrentState = ConstantKeys.CurrentState.STATE_PREPARING;
            // 控制器，更新不同的播放状态的UI
            mController.onPlayStateChanged(mCurrentState);
            DyLog.d("STATE_PREPARING");
        } catch (IOException e) {
            e.printStackTrace();
            DyLog.e("打开播放器发生错误", e);
        }
    }


    @Override
    public void setSpeed(float speed) {
        if (speed<0){
            DyLog.d("speed is not < 0");
        }
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            ((IjkMediaPlayer) mMediaPlayer).setSpeed(speed);
        } else if (mMediaPlayer instanceof AndroidMediaPlayer){
            //((AndroidMediaPlayer) mMediaPlayer).setSpeed(speed);
            DyLog.d("只有IjkPlayer才能设置播放速度");
        }else if(mMediaPlayer instanceof MediaPlayer){
            //((MediaPlayer) mMediaPlayer).setSpeed(speed);
            DyLog.d("只有IjkPlayer才能设置播放速度");
        } else {
            DyLog.d("只有IjkPlayer才能设置播放速度");
        }
    }

    /**
     * 设置视频控制器，必须设置
     * @param controller        AbsVideoPlayerController子类对象，可用VideoPlayerController，也可自定义
     */
    public void setController(@NonNull AbsPlayerController controller) {
        //这里必须先移除
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
     * 设置准备视频播放监听事件
     */
    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            mCurrentState = ConstantKeys.CurrentState.STATE_PREPARED;
            mController.onPlayStateChanged(mCurrentState);
            DyLog.d("onPrepared ——> STATE_PREPARED");
            mp.start();
            // 从上次的保存位置播放  todo
//            if (continueFromLastPosition) {
//                long savedPlayPosition = VideoPlayerUtils.getSavedPlayPosition(mContext, mUrl);
//                mp.seekTo(savedPlayPosition);
//            }
            // 跳到指定位置播放
//            if (skipToPosition != 0) {
//                mp.seekTo(skipToPosition);
//            }
        }
    };

    /**
     * 设置视频播放完成监听事件
     */
    private IMediaPlayer.OnCompletionListener mOnCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_COMPLETED;
                    mController.onPlayStateChanged(mCurrentState);
                    DyLog.d("onCompletion ——> STATE_COMPLETED");
                    // 清除屏幕常亮
                    mContainer.setKeepScreenOn(false);
                }
            };


    /**
     * 设置视频缓冲更新监听事件
     */
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                final int MAX_PERCENT = 97;
                @Override
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    //TODO
                //    mBufferPercentage = percent;
                    //播放完成后再次播放getBufferPercentage获取的值也不准确，94、95，达不到100
               //     if (percent>MAX_PERCENT){
               //         mBufferPercentage = 100;
                //    }
                    DyLog.d("onBufferingUpdate ——> " + percent);
                }
            };


    /**
     * 设置视频seek完成监听事件
     */
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener =
            new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                    DyLog.d("onSeekComplete ——> " );
                }
            };


    /**
     * 设置视频大小更改监听器
     */
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            mTextureView.adaptVideoSize(width, height);
            DyLog.d("onVideoSizeChanged ——> width：" + width + "， height：" + height);
        }
    };

    /**
     * 设置视频错误监听器
     * int MEDIA_INFO_VIDEO_RENDERING_START = 3;//视频准备渲染
     * int MEDIA_INFO_BUFFERING_START = 701;//开始缓冲
     * int MEDIA_INFO_BUFFERING_END = 702;//缓冲结束
     * int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;//视频选择信息
     * int MEDIA_ERROR_SERVER_DIED = 100;//视频中断，一般是视频源异常或者不支持的视频类型。
     * int MEDIA_ERROR_IJK_PLAYER = -10000,//一般是视频源有问题或者数据格式不支持，比如音频不是AAC之类的
     * int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;//数据错误没有有效的回收
     */
    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            // 直播流播放时去调用mediaPlayer.getDuration会导致-38和-2147483648错误，忽略该错误
            if (what != -38 && what != -2147483648 && extra != -38 && extra != -2147483648) {
                mCurrentState = ConstantKeys.CurrentState.STATE_ERROR;
                mController.onPlayStateChanged(mCurrentState);
            }
            DyLog.e("onError ——> STATE_ERROR ———— what：" + what + ", extra: " + extra);
            return true;
        }
    };

    /**
     * 设置视频信息监听器
     */
    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // 播放器开始渲染
                mCurrentState = ConstantKeys.CurrentState.STATE_PLAYING;
                mController.onPlayStateChanged(mCurrentState);
                DyLog.d("onInfo ——> MEDIA_INFO_VIDEO_RENDERING_START：STATE_PLAYING");
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                // MediaPlayer暂时不播放，以缓冲更多的数据
                if (mCurrentState == ConstantKeys.CurrentState.STATE_PAUSED || mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED;
                    DyLog.d("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PAUSED");
                } else {
                    mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING;
                    DyLog.d("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PLAYING");
                }
                mController.onPlayStateChanged(mCurrentState);
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                // 填充缓冲区后，MediaPlayer恢复播放/暂停
                if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_PLAYING;
                    mController.onPlayStateChanged(mCurrentState);
                    DyLog.d("onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PLAYING");
                }
                if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_PAUSED;
                    mController.onPlayStateChanged(mCurrentState);
                    DyLog.d("onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PAUSED");
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                // 视频旋转了extra度，需要恢复
                if (mTextureView != null) {
                    mTextureView.setRotation(extra);
                    DyLog.d("视频旋转角度：" + extra);
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                DyLog.d("视频不能seekTo，为直播视频");
            } else {
                DyLog.d("onInfo ——> what：" + what);
            }
            return true;
        }
    };

    /**
     * 设置时间文本监听器
     */
    private IMediaPlayer.OnTimedTextListener mOnTimedTextListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
            DyLog.d("onTimedText ——> ijkTimedText：" + ijkTimedText.getText());
        }
    };
}
