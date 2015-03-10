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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge;

import android.accounts.Account;
import android.content.Context;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JsInjectorFactory {
    private final Context mContext;

    private JsInjectorFactory(Context context) {
        mContext = context;
    }

    public static JsInjectorFactory getInstance(Context context) {
        return new JsInjectorFactory(context);
    }

    public JsInterfaceInjector createInjector() {
        Account account = JasperAccountManager.get(mContext).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(mContext, account);
        String versionName = accountServerData.getVersionName();
        ServerRelease serverRelease = ServerRelease.parseVersion(accountServerData.getVersionName());

        switch (serverRelease) {
            case EMERALD:
            case EMERALD_MR1:
            case EMERALD_MR2:
            case EMERALD_MR3:
               return new EmeraldJsInterfaceInjector();
            case AMBER:
            case AMBER_MR1:
            case AMBER_MR2:
                return new AmberJsInterfaceInjector();
            default:
                throw new UnsupportedOperationException("Could not identify web js interface injector for current versionName: " + versionName);
        }
    }
}
