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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.flow;

import android.accounts.Account;
import android.content.Context;
import android.webkit.WebView;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.token.BasicAccessTokenEncoder;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.HashMap;

/**
 * @author Tom Koptel
 * @since 2.0
 */
class WebFlowStrategyImpl implements WebFlowStrategy {
    private final AccountServerData mServerData;
    protected final String mUri;
    protected final WebFlow mWebFlow;

    public WebFlowStrategyImpl(Context context, WebFlow webFlow, ResourceLookup resource) {
        Account account = JasperAccountManager.get(context).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(context, account);
        mWebFlow = webFlow;
        mServerData = accountServerData;
        mUri = resource.getUri();
    }

    @Override
    public void load(WebView webView) {
        String serverUrl = mServerData.getServerUrl();
        String flow = serverUrl + mWebFlow.getFlowUri() + mUri;
        webView.loadUrl(flow, getDefaultHeaders());
    }

    private HashMap<String, String> getDefaultHeaders() {
        BasicAccessTokenEncoder tokenEncoder = BasicAccessTokenEncoder.builder()
                .setUsername(mServerData.getUsername())
                .setOrganization(mServerData.getOrganization())
                .setPassword(mServerData.getPassword())
                .build();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Authorization", tokenEncoder.encodeToken());
        return map;
    }
}
