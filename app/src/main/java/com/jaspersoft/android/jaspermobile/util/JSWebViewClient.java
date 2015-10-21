/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import android.accounts.Account;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.webview.DefaultUrlPolicy;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Use {@link com.jaspersoft.android.jaspermobile.webview.DefaultUrlPolicy} together with {@link com.jaspersoft.android.jaspermobile.webview.SystemWebViewClient}
 *
 * @author Tom Koptel
 * @since 1.9
 */
@Deprecated
@EBean
public class JSWebViewClient extends WebViewClient {
    @RootContext
    protected Activity activity;

    private String serverUrl;
    private DefaultUrlPolicy.SessionListener sessionListener;

    @AfterInject
    final void initServerUrl() {
        Account account = JasperAccountManager.get(activity).getActiveAccount();
        AccountServerData serverData = AccountServerData.get(activity, account);
        serverUrl = serverData.getServerUrl();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        String jasperHost = Uri.parse(serverUrl).getHost();
        String linkHost = Uri.parse(url).getHost();

        // This is my Jasper site, let WebView check page for 401 page
        if (linkHost != null && linkHost.equals(jasperHost)) {
            if (url.contains("login.html")) {
                if (sessionListener != null) {
                    sessionListener.onSessionExpired();
                }
                return true;
            }
        }

        // Otherwise, the link is not for us, so launch another Activity that handles URLs
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // show notification if no app available to open selected format
            Toast.makeText(activity,
                    activity.getString(R.string.sdr_t_no_app_available, "view"), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void setSessionListener(DefaultUrlPolicy.SessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }

}
