/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class CordovaInterfaceImpl implements CordovaInterface {
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final OnPageFinishedLsitener finishedLsitener;
    private final WeakReference<Activity> weakReference;

    public CordovaInterfaceImpl(Activity activity) {
        weakReference = new WeakReference<Activity>(activity);
        if (activity instanceof OnPageFinishedLsitener) {
            this.finishedLsitener = (OnPageFinishedLsitener) activity;
        } else {
            this.finishedLsitener = new ShallowListener();
        }
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
    }

    @Override
    public Activity getActivity() {
        return weakReference.get();
    }

    @Override
    public Object onMessage(String message, Object data) {
        if ("onPageFinished".equals(message)) {
            finishedLsitener.onPageFinished();
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        return executorService;
    }

    private static class ShallowListener implements OnPageFinishedLsitener {
        @Override
        public void onPageFinished() {
        }
    }

    public static interface OnPageFinishedLsitener {
        void onPageFinished();
    }
}
