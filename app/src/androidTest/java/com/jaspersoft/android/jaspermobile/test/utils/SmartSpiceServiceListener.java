package com.jaspersoft.android.jaspermobile.test.utils;

import android.util.Log;

import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class SmartSpiceServiceListener implements SpiceServiceListener {
    protected static final String TAG = "CountingIdlingResource";

    private final boolean mDebug;

    protected SmartSpiceServiceListener(boolean mDebug) {
        this.mDebug = mDebug;
        if (mDebug) {
            logd(TAG, String.format("CustomSpiceServerListener " + this.hashCode() + "registered"));
        }
    }

    protected abstract void incrementIdleResource(CachedSpiceRequest<?> request);

    protected abstract void decrementIdleResource(CachedSpiceRequest<?> request);

    @Override
    public void onRequestFailed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        logd(TAG, "onRequestFailed");
        decrementIdleResource(request);
    }

    @Override
    public void onRequestCancelled(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        logd(TAG, "onRequestCancelled");
        decrementIdleResource(request);
    }

    @Override
    public void onRequestProgressUpdated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        logd(TAG, "onRequestProgressUpdated");
    }

    @Override
    public void onRequestSucceeded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        dumpLog("onRequestSucceeded", request.getResultType().getSimpleName(), request.hashCode());
        decrementIdleResource(request);
    }

    @Override
    public void onRequestAdded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        dumpLog("onRequestAdded", request.getResultType().getSimpleName(), request.hashCode());
        incrementIdleResource(request);
    }

    @Override
    public void onRequestAggregated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        logd(TAG, "onRequestAggregated");
    }

    @Override
    public void onRequestNotFound(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        logd(TAG, "onRequestNotFound");
        decrementIdleResource(request);
    }

    @Override
    public void onRequestProcessed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
        logd(TAG, "onRequestProcessed");
        decrementIdleResource(request);
    }

    @Override
    public void onServiceStopped() {
        logd(TAG, "onServiceStopped");
    }
    
    private void dumpLog(String tag, String what, long whatHashCode) {
        dumpLog(tag, what, whatHashCode, "");
    }

    private void dumpLog(String tag, String what, long whatHashCode, String extraMsg) {
        logd(TAG, String.format("CustomSpiceServerListener %s for: %s %d %s", tag, what, whatHashCode, extraMsg));
    }

    protected void logd(String tag, String message) {
        if (mDebug) {
            Log.d(tag, message);
        }
    }
}
