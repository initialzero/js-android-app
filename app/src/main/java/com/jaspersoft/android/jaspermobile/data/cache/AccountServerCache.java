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
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implements server cache on the basis of {@link AccountManager}.
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class AccountServerCache implements ServerCache {
    public static final String SERVER_URL_KEY = "SERVER_URL_KEY";
    public static final String EDITION_KEY = "EDITION_KEY";
    public static final String VERSION_NAME_KEY = "VERSION_NAME_KEY";

    private final AccountManager mAccountManager;
    private final AccountDataMapper mAccountDataMapper;
    private final ProfileCache mProfileAccountCache;

    @Inject
    public AccountServerCache(AccountManager accountManager,
                              AccountDataMapper accountDataMapper,
                              ProfileCache profileAccountCache) {
        mAccountManager = accountManager;
        mAccountDataMapper = accountDataMapper;
        mProfileAccountCache = profileAccountCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(Profile profile, JasperServer jasperServer) {
        Account accountProfile = mAccountDataMapper.transform(profile);
        mAccountManager.setUserData(accountProfile, SERVER_URL_KEY, jasperServer.getBaseUrl());
        mAccountManager.setUserData(accountProfile, EDITION_KEY, String.valueOf(jasperServer.isProEdition()));
        mAccountManager.setUserData(accountProfile, VERSION_NAME_KEY, String.valueOf(jasperServer.getVersionName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JasperServer get(Profile profile) {
        Account accountProfile = mAccountDataMapper.transform(profile);

        String baseUrl = mAccountManager.getUserData(accountProfile, SERVER_URL_KEY);
        String edition = mAccountManager.getUserData(accountProfile, EDITION_KEY);
        String versionString = mAccountManager.getUserData(accountProfile, VERSION_NAME_KEY);

        JasperServer.Builder serverBuilder = JasperServer.builder();
        serverBuilder.setBaseUrl(baseUrl);
        serverBuilder.setEditionIsPro(Boolean.valueOf(edition));
        serverBuilder.setVersion(ServerVersion.valueOf(versionString));

        return serverBuilder.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasServer(Profile profile) {
        if (mProfileAccountCache.hasProfile(profile)) {
            Account accountProfile = mAccountDataMapper.transform(profile);

            String baseUrl = mAccountManager.getUserData(accountProfile, SERVER_URL_KEY);
            String edition = mAccountManager.getUserData(accountProfile, EDITION_KEY);
            String versionString = mAccountManager.getUserData(accountProfile, VERSION_NAME_KEY);

            return !(TextUtils.isEmpty(baseUrl) && TextUtils.isEmpty(edition) && TextUtils.isEmpty(versionString));
        }
        return false;
    }
}
