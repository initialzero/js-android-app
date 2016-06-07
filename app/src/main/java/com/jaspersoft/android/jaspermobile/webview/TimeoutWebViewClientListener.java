/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.webview;

import android.os.Handler;
import android.webkit.WebViewClient;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class TimeoutWebViewClientListener implements JasperWebViewClientListener {
    private static final long DEFAULT_TIMEOUT = 60000L;

    private final Handler timeoutHandler = new Handler();
    private final JasperWebViewClientListener mDelegate;
    private long mTimeout;
    private Runnable timeoutRunnable;

    private TimeoutWebViewClientListener(JasperWebViewClientListener webViewClientListener) {
        mTimeout = DEFAULT_TIMEOUT;
        mDelegate = webViewClientListener;
    }

    public static TimeoutWebViewClientListener wrap(JasperWebViewClientListener webViewClientListener) {
        if (webViewClientListener == null) {
            throw new IllegalArgumentException("Delegate listener should not be null");
        }
        return new TimeoutWebViewClientListener(webViewClientListener);
    }

    public TimeoutWebViewClientListener withTimeout(long timeout) {
        mTimeout = timeout;
        return this;
    }

    @Override
    public void onPageStarted(String newUrl) {
        timeoutRunnable = new TimeoutRunnable(newUrl);
        timeoutHandler.postDelayed(timeoutRunnable, mTimeout);
        mDelegate.onPageStarted(newUrl);
    }

    @Override
    public void onReceivedError(int errorCode, String description, String failingUrl) {
        mDelegate.onReceivedError(errorCode, description, failingUrl);
    }

    @Override
    public void onPageFinishedLoading(String url) {
        timeoutHandler.removeCallbacks(timeoutRunnable);
        mDelegate.onPageFinishedLoading(url);
    }

    private class TimeoutRunnable implements Runnable {
        private final String url;

        private TimeoutRunnable(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            onReceivedError(WebViewClient.ERROR_TIMEOUT, null, url);
            timeoutHandler.removeCallbacks(this);
        }
    }
}
