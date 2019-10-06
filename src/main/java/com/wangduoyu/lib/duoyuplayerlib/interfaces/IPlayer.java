package com.wangduoyu.lib.duoyuplayerlib.interfaces;

import java.util.Map;

public interface IPlayer {

    /**
     * 设置视频Url，以及headers
     *
     * @param url           视频地址，可以是本地，也可以是网络视频
     * @param headers       请求header.
     */
    void setUp(String url, Map<String, String> headers);

    /**
     * 开始播放
     */
    void start();

    /**
     * 从指定的位置开始播放
     *
     * @param position      播放位置
     */
    void start(long position);



    /**
     * 设置播放速度，目前只有IjkPlayer有效果，原生MediaPlayer暂不支持
     *
     * @param speed 播放速度
     */
    void setSpeed(float speed);

}
