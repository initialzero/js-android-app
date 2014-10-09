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

package com.jaspersoft.android.jaspermobile.test.acceptance.viewer;

import android.app.Activity;
import android.content.ComponentName;
import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleCallback;
import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.IdlingPolicies;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.fragment.WebViewFragment;
import com.jaspersoft.android.jaspermobile.test.utils.IdleInjector;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;

import java.util.concurrent.TimeUnit;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class WebViewInjector implements ActivityLifecycleCallback {
    private final WebViewIdlingResource mWebViewIdlingResource;
    private final Class<? extends FragmentActivity> mClass;
    private boolean mInjected;

    public WebViewInjector(Class<? extends FragmentActivity> clazz, WebViewIdlingResource webViewIdlingResource) {
        mWebViewIdlingResource = webViewIdlingResource;
        mClass = clazz;
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        ComponentName targetComponentName =
                new ComponentName(activity, mClass.getName());

        ComponentName currentComponentName = activity.getComponentName();
        if (!currentComponentName.equals(targetComponentName)) return;

        switch (stage) {
            case RESUMED:
                // As soon as, we are trying to register inject of idle resource during on Resume.
                // We need to do this only first time. And yes, I know it is dirty. Any suggestions welcomed :)
                if (!mInjected) {
                    FragmentActivity htmlViewerActivity = (FragmentActivity) activity;
                    WebViewFragment fragment = (WebViewFragment) htmlViewerActivity.getSupportFragmentManager()
                            .findFragmentByTag(WebViewFragment.TAG);
                    ViewGroup holder = (ViewGroup) fragment.getView().findViewById(R.id.webViewPlaceholder);
                    // We need to wait for the activity to be created before getting a reference
                    // to the webview
                    JSWebView webView = (JSWebView) holder.getChildAt(0);

                    mWebViewIdlingResource.inject(webView);
                    mInjected = true;
                }
                break;
            case STOPPED:
                // Clean up reference
                if (activity.isFinishing()) mWebViewIdlingResource.clear();
                break;
            default: // NOP
        }
    }

    public static IdleInjector registerFor(Class<? extends FragmentActivity> clazz) {
        IdlingPolicies.setIdlingResourceTimeout(150, TimeUnit.SECONDS);
        WebViewIdlingResource webViewIdlingResource = new WebViewIdlingResource();
        Espresso.registerIdlingResources(webViewIdlingResource);

        WebViewInjector injector = new WebViewInjector(clazz, webViewIdlingResource);
        ActivityLifecycleMonitorRegistry.getInstance()
                .addLifecycleCallback(injector);
        return new InjectorHolder(injector);
    }

    private static class InjectorHolder implements IdleInjector {
        private final WebViewInjector injector;

        private InjectorHolder(WebViewInjector injector) {
            this.injector = injector;
        }

        @Override
        public void unregister() {
            ActivityLifecycleMonitorRegistry.getInstance()
                    .removeLifecycleCallback(injector);
        }
    }
}
