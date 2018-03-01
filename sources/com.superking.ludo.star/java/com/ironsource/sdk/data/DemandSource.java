package com.ironsource.sdk.data;

import com.ironsource.sdk.listeners.OnRewardedVideoListener;
import java.util.Map;

public class DemandSource {
    public static final int INIT_FAILED = 3;
    public static final int INIT_NOT_STARTED = 0;
    public static final int INIT_PENDING = 1;
    public static final int INIT_SUCCEEDED = 2;
    private Map<String, String> mExtraParams;
    private int mInitState = INIT_NOT_STARTED;
    private OnRewardedVideoListener mListener;
    private String mName;

    public DemandSource(String demandSourceName, Map<String, String> extraParams, OnRewardedVideoListener listener) {
        this.mName = demandSourceName;
        this.mExtraParams = extraParams;
        this.mListener = listener;
    }

    public String getDemandSourceName() {
        return this.mName;
    }

    public int getDemandSourceInitState() {
        return this.mInitState;
    }

    public Map<String, String> getExtraParams() {
        return this.mExtraParams;
    }

    public synchronized void setDemandSourceInitState(int initState) {
        this.mInitState = initState;
    }

    public OnRewardedVideoListener getListener() {
        return this.mListener;
    }
}
