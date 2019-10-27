package com.wangduoyu.lib.duoyuplayerlib.cache;

import com.danikula.videocache.headers.HeaderInjector;
import com.wangduoyu.lib.duoyuplayerlib.utils.DuoYuLog;

import java.util.HashMap;
import java.util.Map;

/**
 for android video cache header
 */
public class ProxyCacheUserAgentHeadersInjector implements HeaderInjector {

    public final static Map<String, String> mMapHeadData = new HashMap<>();

    @Override
    public Map<String, String> addHeaders(String url) {
        DuoYuLog.d("****** proxy addHeaders ****** " + mMapHeadData.size());
        return mMapHeadData;
    }
}
