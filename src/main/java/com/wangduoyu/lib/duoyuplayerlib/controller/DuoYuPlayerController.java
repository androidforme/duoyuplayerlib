package com.wangduoyu.lib.duoyuplayerlib.controller;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.wangduoyu.lib.duoyuplayerlib.R;

public class DuoYuPlayerController extends AbsPlayerController {

    public DuoYuPlayerController(@NonNull Context context) {
        super(context);
        init();
    }

    /**
     * 初始化操作
     */
    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.dy_video_player, this, true);
        //initFindViewById();
        //initListener();
       // registerNetChangedReceiver();
    }

    @Override
    public void reset() {

    }

    @Override
    public void onPlayStateChanged(int mCurrentState) {

    }
}
