/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities;

import android.content.Context;
import android.os.Bundle;
import android.webkit.WebViewClient;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.WebViewFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.WebViewFragment_;
import com.jaspersoft.android.jaspermobile.network.PrivacyRequest;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EActivity
public class PrivacyPolicyActivity extends RoboSpiceActivity implements WebViewFragment.OnWebViewCreated {

    private WebViewFragment webViewFragment;

    @Inject
    PrivacyRequest request;

    @Bean
    DefaultPrefHelper prefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            webViewFragment = WebViewFragment_.builder()
                    .resourceLabel(getString(R.string.sa_about_privacy))
                    .build();

            webViewFragment.setOnWebViewCreated(this);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, webViewFragment, WebViewFragment.TAG)
                    .commit();
        }
        else {
            webViewFragment = (WebViewFragment) getSupportFragmentManager()
                    .findFragmentByTag(WebViewFragment.TAG);
        }
    }

    @Override
    public void onWebViewCreated(final WebViewFragment webViewFragment) {
        webViewFragment.getWebView().setWebViewClient(new WebViewClient());

        getSpiceManager().getFromCacheAndLoadFromNetworkIfExpired(request,
                request.createCacheKey(), prefHelper.getRepoCacheExpirationValue(),
                new PrivacyRequestListener());
    }

    private class PrivacyRequestListener extends SimpleRequestListener<String> {

        @Override
        protected Context getContext() {
            return PrivacyPolicyActivity.this;
        }

        @Override
        public void onRequestSuccess(String privacy) {
            if (privacy == null) {
                return;
            }
            webViewFragment.loadHtml(PrivacyRequest.PRIVACY_URL, privacy);
        }
    }

}
