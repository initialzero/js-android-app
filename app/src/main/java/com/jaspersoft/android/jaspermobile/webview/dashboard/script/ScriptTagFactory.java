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

package com.jaspersoft.android.jaspermobile.webview.dashboard.script;

import android.accounts.Account;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.webview.ScriptTagCreator;
import com.jaspersoft.android.jaspermobile.webview.dashboard.DashboardRequestInterceptor;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ScriptTagFactory {
    private final Context mContext;

    private ScriptTagFactory(Context context) {
        mContext = context;
    }

    public static ScriptTagFactory getInstance(Context context) {
        return new ScriptTagFactory(context);
    }

    public ScriptTagCreator getTagCreator(ResourceLookup resource) {
        String token = DashboardRequestInterceptor.INJECTION_TOKEN;
        if (resource.getResourceType() == ResourceLookup.ResourceType.legacyDashboard) {
            return new EmeraldDashboardScriptTagCreator(token);
        }

        Account account = JasperAccountManager.get(mContext).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(mContext, account);
        String versionName = accountServerData.getVersionName();
        ServerRelease serverRelease = ServerRelease.parseVersion(accountServerData.getVersionName());

        switch (serverRelease) {
            case EMERALD:
            case EMERALD_MR1:
            case EMERALD_MR2:
            case EMERALD_MR3:
            case EMERALD_MR4:
                return new EmeraldDashboardScriptTagCreator(token);
            case AMBER:
            case AMBER_MR1:
                return new AmberDashboardScriptTagCreator(token);
            case AMBER_MR2:
                return new Amber2DashboardScriptTagCreator(token);
            default:
                throw new UnsupportedOperationException("Could not script creator for current versionName: " + versionName);
        }
    }
}
