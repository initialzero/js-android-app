package com.jaspersoft.android.jaspermobile.webview;

import android.webkit.WebView;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class WebInterface {
    private final Deque<Runnable> taskQueue = new ArrayDeque<Runnable>();
    private boolean mPaused;

    public abstract void exposeJavascriptInterface(WebView webView);

    public void pause() {
        mPaused = true;
    }

    public void resume() {
        mPaused = false;
        while (!taskQueue.isEmpty()) {
            dispatch(taskQueue.pollFirst());
        }
    }

    protected void handleCallback(Runnable runnable) {
        if (mPaused) {
            conserve(runnable);
        } else {
            dispatch(runnable);
        }
    }

    private void dispatch(Runnable runnable) {
        runnable.run();
    }

    private void conserve(Runnable runnable) {
        taskQueue.add(runnable);
    }
}
