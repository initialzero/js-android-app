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

package com.jaspersoft.android.jaspermobile.data.cache;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.server.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class JasperServerCacheImpl implements JasperServerCache {
    public static final String SERVER_URL_KEY = "SERVER_URL_KEY";
    public static final String EDITION_KEY = "EDITION_KEY";
    public static final String VERSION_NAME_KEY = "VERSION_NAME_KEY";
    private final Context mContext;
    private final String mAccountType;

    @Inject
    public JasperServerCacheImpl(Context context, String accountType) {
        mContext = context;
        mAccountType = accountType;
    }

    @Override
    public void put(Profile profile, JasperServer jasperServer) {
        AccountManager accountManager = AccountManager.get(mContext);
        Account accountProfile = new Account(profile.getKey(), mAccountType);
        accountManager.setUserData(accountProfile, SERVER_URL_KEY, jasperServer.getBaseUrl());
        accountManager.setUserData(accountProfile, EDITION_KEY, jasperServer.getEdition());
        accountManager.setUserData(accountProfile, VERSION_NAME_KEY, String.valueOf(jasperServer.getVersion()));
    }
}
