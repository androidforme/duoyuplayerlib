package com.wangduoyu.lib.duoyuplayerlib.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wangduoyu.lib.duoyuplayerlib.interfaces.IPlayer;

/**
 * 控制器抽象类
 */
public abstract class AbsPlayerController extends FrameLayout {
    public Context mContext;
    protected IPlayer mPlayer;

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

}
