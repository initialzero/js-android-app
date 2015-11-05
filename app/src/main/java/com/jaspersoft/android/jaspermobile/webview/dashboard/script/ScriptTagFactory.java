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

package com.jaspersoft.android.jaspermobile.webview.dashboard.script;

import android.accounts.Account;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.webview.ScriptTagCreator;
import com.jaspersoft.android.jaspermobile.webview.dashboard.InjectionRequestInterceptor;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

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
        String token = InjectionRequestInterceptor.INJECTION_TOKEN;
        if (resource.getResourceType() == ResourceLookup.ResourceType.legacyDashboard) {
            return new EmeraldDashboardScriptTagCreator(token);
        }

        Account account = JasperAccountManager.get(mContext).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(mContext, account);
        String versionName = accountServerData.getVersionName();
        ServerVersion serverVersion = ServerVersion.defaultParser().parse(accountServerData.getVersionName());

        if (serverVersion.getVersionCode() <= ServerVersion.EMERALD_MR4.getVersionCode()) {
            return new EmeraldDashboardScriptTagCreator(token);
        } else if (serverVersion.getVersionCode() >= ServerVersion.AMBER.getVersionCode() &&
                serverVersion.getVersionCode() <= ServerVersion.AMBER_MR1.getVersionCode()) {
            return new AmberDashboardScriptTagCreator(token);
        } else if (serverVersion.getVersionCode() >= ServerVersion.AMBER_MR2.getVersionCode()) {
            return new Amber2DashboardScriptTagCreator(token);
        } else {
            throw new UnsupportedOperationException("Could not script creator for current versionName: " + versionName);
        }
   }
}
