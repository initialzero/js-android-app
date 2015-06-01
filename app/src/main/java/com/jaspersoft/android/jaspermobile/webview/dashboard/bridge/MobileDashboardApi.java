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

package com.jaspersoft.android.jaspermobile.webview.dashboard.bridge;

import android.accounts.Account;
import android.content.Context;
import android.webkit.WebView;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Introduces hardcoded Javascript calls.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class MobileDashboardApi implements DashboardApi {
    private final WebView webView;

    private MobileDashboardApi(WebView webView) {
        this.webView = webView;
    }

    public static MobileDashboardApi with(WebView webView) {
        if (webView == null) {
            throw new IllegalArgumentException("WebView reference should not be null");
        }
        return new MobileDashboardApi(webView);
    }

    public void refreshDashlet() {
        webView.loadUrl(assembleUri("MobileDashboard.refresh()"));
    }

    public void minimizeDashlet() {
        webView.loadUrl(assembleUri("MobileDashboard.refreshDashlet()"));
    }

    @Override
    public void pause() {
        webView.loadUrl(assembleUri("MobileDashboard.pause()"));
    }

    @Override
    public void resume() {
        webView.loadUrl(assembleUri("MobileDashboard.resume()"));
    }

    @Override
    public void refreshDashboard() {
        webView.loadUrl(assembleUri("MobileDashboard.refresh()"));
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @Override
    public void run(String uri, double diagonal) {
        StringBuilder builder = new StringBuilder();
        builder.append("javascript:MobileDashboard")
                .append(".configure({ \"diagonal\": %s })")
                .append(".run({ \"uri\": \"%s\" })");
        String executeScript = String.format(builder.toString(), diagonal, uri);
        webView.loadUrl(executeScript);
    }

    private String assembleUri(String command) {
        return "javascript:" + command;
    }
}
