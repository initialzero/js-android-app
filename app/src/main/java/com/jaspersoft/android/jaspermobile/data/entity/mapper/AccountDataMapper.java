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

package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import android.accounts.Account;

import com.jaspersoft.android.jaspermobile.domain.Profile;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class AccountDataMapper {
    private final String mAccountType;

    @Inject
    public AccountDataMapper(@Named("accountType") String accountType) {
        mAccountType = accountType;
    }

    public String getAccountType() {
        return mAccountType;
    }

    public Account transform(Profile profile) {
        String name = profile.getKey();
        return new Account(name, mAccountType);
    }

    public List<Profile> transform(Account[] accounts) {
        List<Profile> profiles = new ArrayList<>(accounts.length);
        for (Account account : accounts) {
            if (account != null) {
                Profile profile = transform(account);
                profiles.add(profile);
            }
        }
        return profiles;
    }

    public Profile transform(Account account) {
        return Profile.create(account.name);
    }
}
