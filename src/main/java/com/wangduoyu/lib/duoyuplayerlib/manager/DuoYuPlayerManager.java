package com.wangduoyu.lib.duoyuplayerlib.manager;

import com.wangduoyu.lib.duoyuplayerlib.interfaces.IPlayer;

public class DuoYuPlayerManager {

    private IPlayer mPlayer;

    private DuoYuPlayerManager() {
    }

    private static DuoYuPlayerManager sInstance;

    public static synchronized DuoYuPlayerManager getInstance() {
        if (sInstance == null) {
            sInstance = new DuoYuPlayerManager();
        }
        return sInstance;
    }

    public IPlayer getCurrentPlayer() {
        return mPlayer;
    }

    public void setCurrentPlayer(IPlayer player) {
        if (mPlayer != player) {
            releasePlayer();
            mPlayer = player;
        }
    }

//    public void suspendNiceVideoPlayer() {
//        if (mPlayer != null && (mPlayer.isPlaying() || mPlayer.isBufferingPlaying())) {
//            mPlayer.pause();
//        }
//    }
//
//    public void resumeNiceVideoPlayer() {
//        if (mPlayer != null && (mPlayer.isPaused() || mPlayer.isBufferingPaused())) {
//            mPlayer.restart();
//        }
//    }
//
    public void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
//
//    public boolean onBackPressd() {
//        if (mPlayer != null) {
//            if (mPlayer.isFullScreen()) {
//                return mPlayer.exitFullScreen();
//            } else if (mPlayer.isTinyWindow()) {
//                return mPlayer.exitTinyWindow();
//            }
//        }
//        return false;
//    }
}
