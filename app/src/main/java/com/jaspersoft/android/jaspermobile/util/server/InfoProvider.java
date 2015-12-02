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

package com.jaspersoft.android.jaspermobile.util.server;

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.androidannotations.annotations.EBean;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@EBean
public class InfoProvider implements ServerInfoProvider {
    private AccountServerData serverData;

    public InfoProvider(Context context) {
        JasperAccountManager accountManager = JasperAccountManager.get(context);
        Account account = accountManager.getActiveAccount();
        serverData = AccountServerData.get(context, account);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ServerVersion getVersion() {
        return ServerVersion.valueOf(serverData.getVersionName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProEdition() {
        return Boolean.valueOf(serverData.getEdition());
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getOrganization() {
        return TextUtils.isEmpty(serverData.getOrganization()) ? "" : serverData.getOrganization();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getUsername() {
        return serverData.getUsername();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getPassword() {
        return serverData.getPassword();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getAlias() {
        return serverData.getAlias();
    }

    @NonNull
    @Override
    public String getServerUrl() {
        return serverData.getServerUrl();
    }
}
