package com.wangduoyu.lib.duoyuplayerlib.player;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wangduoyu.lib.duoyuplayerlib.controller.AbsPlayerController;
import com.wangduoyu.lib.duoyuplayerlib.interfaces.IPlayer;
import com.wangduoyu.lib.duoyuplayerlib.utils.DyLog;

import java.util.Map;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

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

    private IMediaPlayer mMediaPlayer;
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

    }

    @Override
    public void start(long position) {

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
}
