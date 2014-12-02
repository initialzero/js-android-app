/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile SDK for Android.
 *
 * Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import android.app.Activity;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class JSWebViewClient extends WebViewClient {
    @RootContext
    Activity activity;
    @Inject
    JsRestClient jsRestClient;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(activity);
        injector.injectMembersWithoutViews(this);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        String serverUrl = jsRestClient.getServerProfile().getServerUrl();
        String jasperHost = Uri.parse(serverUrl).getHost();

        // This is my Jasper site, let WebView load the page with additional parameter
        if (Uri.parse(url).getHost().equals(jasperHost)) {
            List<UrlQuerySanitizer.ParameterValuePair> parametersList
                    = new UrlQuerySanitizer(url).getParameterList();
            if (parametersList.isEmpty()) {
                url += "?";
            }
            url += "&decorate=no";
            view.loadUrl(url);
            return true;
        }

        return false;
    }

}
