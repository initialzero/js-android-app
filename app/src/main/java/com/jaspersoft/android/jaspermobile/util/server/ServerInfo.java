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

package com.jaspersoft.android.jaspermobile.util.server;

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class ServerInfo implements ServerInfoProvider {
    private final AccountServerData serverData;

    private ServerInfo(Context context) {
        JasperAccountManager accountManager = JasperAccountManager.get(context);
        Account account = accountManager.getActiveAccount();
        serverData = AccountServerData.get(context, account);
    }

    public static ServerInfoProvider newInstance(Context context) {
        return new ServerInfo(context);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getServerVersion() {
        return serverData.getVersionName();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getServerEdition() {
        return serverData.getEdition();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getOrganization() {
        return serverData.getOrganization();
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
    public String getAlias() {
        return serverData.getAlias();
    }
}
