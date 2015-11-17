/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

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
        dispatchAll();
    }

    protected void handleCallback(Runnable runnable) {
        if (mPaused) {
            conserve(runnable);
        } else {
            dispatch(runnable);
        }
    }

    private void dispatchAll() {
        while (!taskQueue.isEmpty() && !mPaused) {
            dispatch(taskQueue.pollFirst());
        }
    }

    private void dispatch(Runnable runnable) {
        runnable.run();
    }

    private void conserve(Runnable runnable) {
        taskQueue.add(runnable);
    }
}
