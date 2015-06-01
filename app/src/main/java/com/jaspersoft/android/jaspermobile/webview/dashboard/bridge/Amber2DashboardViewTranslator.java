/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.webview.dashboard.bridge;

import android.accounts.Account;
import android.content.Context;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.util.ScreenUtil;
import com.jaspersoft.android.jaspermobile.util.ScreenUtil_;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class Amber2DashboardViewTranslator implements DashboardViewTranslator {
    private final WebView webView;
    private final String uri;
    private boolean mLoaded, mExecuted;

    private Amber2DashboardViewTranslator(Builder builder) {
        this.webView = builder.webView;
        this.uri = builder.resource.getUri();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void load() {
        InputStream stream = null;
        Context context = webView.getContext();

        Account account = JasperAccountManager.get(context).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(context, account);

        try {
            stream = context.getAssets().open("dashboard.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");

            Map<String, String> data = new HashMap<String, String>();
            data.put("visualize_url", accountServerData.getServerUrl() + "/client/visualize.js?_opt=true&_showInputControls=true");
            Template tmpl = Mustache.compiler().compile(writer.toString());
            String html = tmpl.execute(data);

            webView.loadDataWithBaseURL(accountServerData.getServerUrl(), html, "text/html", "utf-8", null);
            mLoaded = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @Override
    public void run() {
        ScreenUtil screenUtil = ScreenUtil_.getInstance_(webView.getContext());
        StringBuilder builder = new StringBuilder();
        builder.append("javascript:MobileDashboard")
                .append(".configure({ \"diagonal\": %s })")
                .append(".run({ \"uri\": \"%s\" })");
        String executeScript = String.format(builder.toString(), screenUtil.getDiagonal(), uri);
        webView.loadUrl(executeScript);
        mExecuted = true;
    }

    @Override
    public void pause() {
        if (!mLoaded) {
            Timber.d("Dashboard is not loaded. Can't pause.");
            return;
        }
        if (!mExecuted) {
            Timber.d("Dashboard is not executed. Can't pause.");
            return;
        }
        webView.loadUrl(assembleUri("MobileDashboard.pause()"));
    }

    @Override
    public void resume() {
        if (!mLoaded) {
            Timber.d("Dashboard is not loaded. Can't resume.");
            return;
        }
        if (!mExecuted) {
            Timber.d("Dashboard is not executed. Can't resume.");
            return;
        }
        webView.loadUrl(assembleUri("MobileDashboard.resume()"));
    }

    private String assembleUri(String command) {
        return "javascript:" + command;
    }

    public static class Builder {
        private WebView webView;
        private ResourceLookup resource;

        public Builder webView(WebView webView) {
            this.webView = webView;
            return this;
        }

        public Builder resource(ResourceLookup resource) {
            this.resource = resource;
            return this;
        }

        public DashboardViewTranslator build() {
            if (webView == null) {
                throw new IllegalArgumentException("WebView reference should not be null");
            }
            if (resource == null) {
                throw new IllegalArgumentException("ResourceLookup reference should not be null");
            }
            return new Amber2DashboardViewTranslator(this);
        }
    }
}
