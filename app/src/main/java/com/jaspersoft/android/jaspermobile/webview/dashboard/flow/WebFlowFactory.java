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

package com.jaspersoft.android.jaspermobile.webview.dashboard.flow;

import android.accounts.Account;
import android.content.Context;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public final class WebFlowFactory {
    private final Context mContext;

    private WebFlowFactory(Context context) {
        mContext = context;
    }

    public static WebFlowFactory getInstance(Context context) {
        return new WebFlowFactory(context);
    }

    public WebFlowStrategy createFlow(ResourceLookup resource) {
        Account account = JasperAccountManager.get(mContext).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(mContext, account);
        return createFlow(accountServerData.getVersionName(), resource);
    }

    /**
     * Creates most appropriate strategy
     *
     * @param versionName represents double string to identify current JRS version
     * @return specific web flow strategy which varies between JRS releases
     */
    public WebFlowStrategy createFlow(String versionName, ResourceLookup resource) {
        WebFlow webFlow;
        ServerRelease serverRelease = ServerRelease.parseVersion(versionName);

        if (resource.getResourceType() == ResourceLookup.ResourceType.legacyDashboard) {
            webFlow = new EmeraldWebFlow();
        } else {
            switch (serverRelease) {
                case EMERALD:
                case EMERALD_MR1:
                case EMERALD_MR2:
                case EMERALD_MR3:
                    webFlow = new EmeraldWebFlow();
                    break;
                case AMBER:
                case AMBER_MR1:
                case AMBER_MR2:
                    webFlow = new AmberWebFlow();
                    break;
                default:
                    throw new UnsupportedOperationException("Could not identify web flow strategy for current versionName: " + versionName);
            }
        }

        return new WebFlowStrategyImpl(mContext, webFlow, resource);
    }
}
