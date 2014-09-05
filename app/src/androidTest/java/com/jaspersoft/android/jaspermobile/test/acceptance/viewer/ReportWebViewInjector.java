/*
* Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.acceptance.viewer;

import android.app.Activity;
import android.content.ComponentName;
import android.view.ViewGroup;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleCallback;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ReportWebViewInjector implements ActivityLifecycleCallback {
    private final WebViewIdlingResource webViewIdlingResource;

    public ReportWebViewInjector(WebViewIdlingResource webViewIdlingResource) {
        this.webViewIdlingResource = webViewIdlingResource;
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        ComponentName targetComponentName =
                new ComponentName(activity, ReportHtmlViewerActivity.class.getName());

        ComponentName currentComponentName = activity.getComponentName();
        if (!currentComponentName.equals(targetComponentName)) return;

        switch (stage) {
            case CREATED:
                // We need to wait for the activity to be created before getting a reference
                // to the webview
                ViewGroup holder = (ViewGroup) activity.findViewById(com.jaspersoft.android.jaspermobile.R.id.webViewPlaceholder);
                JSWebView webView = (JSWebView) holder.getChildAt(0);

                webViewIdlingResource.inject(webView);
                break;
            case STOPPED:
                // Clean up reference
                if (activity.isFinishing()) webViewIdlingResource.clear();
                break;
            default: // NOP
        }
    }

    public static ReportWebViewInjector create() {
        WebViewIdlingResource webViewIdlingResource = new WebViewIdlingResource();
        Espresso.registerIdlingResources(webViewIdlingResource);
        return new ReportWebViewInjector(webViewIdlingResource);
    }
}
