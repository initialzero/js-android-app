/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jasperforge.org/projects/androidmobile
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

package com.jaspersoft.android.jaspermobile.activities;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.webkit.*;
import android.widget.ProgressBar;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.JsRestClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class ReportHtmlViewerActivity extends RoboActivity {

    // Extras
    public static final String EXTRA_REPORT_FILE_URI = "ReportHtmlViewerActivity.EXTRA_REPORT_FILE_URI";

    public static final String HTTP_SESSION_ID_NAME = "JSESSIONID";
    
    @InjectView(R.id.report_webView)                private WebView webView;
    @InjectView(R.id.report_webView_progressBar)    private ProgressBar progressBar;

    @Inject
    protected JsRestClient jsRestClient;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_html_viewer_layout);

        // prepare WebView
        webView.getSettings().setPluginsEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // hide progress bar after page load
                progressBar.setVisibility(View.GONE);
            }
        });

        if (jsRestClient.getRestApiDescriptor().getVersion() > 1 ) {
            // Set Cookies
            HttpComponentsClientHttpRequestFactory requestFactory = (HttpComponentsClientHttpRequestFactory) jsRestClient.getRestTemplate().getRequestFactory();
            DefaultHttpClient httpClient = (DefaultHttpClient) requestFactory.getHttpClient();

            Cookie sessionCookie = null;
            for(Cookie cookie : httpClient.getCookieStore().getCookies()) {
                if(cookie.getName().equalsIgnoreCase(HTTP_SESSION_ID_NAME)) {
                    sessionCookie = cookie;
                }
            }

            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
            if (sessionCookie != null) {
                cookieManager.removeSessionCookie();
                SystemClock.sleep(500);
                String cookieString = sessionCookie.getName() + "=" + sessionCookie.getValue() + "; domain=" + sessionCookie.getDomain();
                cookieManager.setCookie(sessionCookie.getDomain(), cookieString);
                CookieSyncManager.getInstance().sync();
            }
        }

        //get report file uri from the intent extras
        String uri = getIntent().getExtras().getString(EXTRA_REPORT_FILE_URI);

        // load the report file from the cache folder
        webView.loadUrl(uri);
    }

}
