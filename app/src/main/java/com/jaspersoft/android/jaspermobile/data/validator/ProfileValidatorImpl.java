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

package com.jaspersoft.android.jaspermobile.data.validator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.ProfileValidationFactory;
import com.jaspersoft.android.jaspermobile.domain.validator.Validation;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class ProfileValidatorImpl implements ProfileValidationFactory {
    private final Context mContext;
    private final String mAccountType;

    @Inject
    public ProfileValidatorImpl(Context context,
                                @Named("accountType") String accountType) {
        mContext = context;
        mAccountType = accountType;
    }

    @Override
    public Validation<DuplicateProfileException> create(Profile profile) {
        final String profileName = profile.getKey();
        final String[] availableNames = getAvailableNames();

        return new Validation<DuplicateProfileException>() {
            @Override
            public DuplicateProfileException getCheckedException() {
                return new DuplicateProfileException(profileName, availableNames);
            }

            @Override
            public boolean perform() {
                Account profileAccount = new Account(profileName, mAccountType);
                return !getAccountSet().contains(profileAccount);
            }
        };
    }

    private Set<Account> getAccountSet() {
        return new HashSet<>(Arrays.asList(getAccounts()));
    }

    private String[] getAvailableNames() {
        Account[] accounts = getAccounts();
        String[] names = new String[accounts.length];
        int count = names.length;
        for (int i = 0; i < count; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    public Account[] getAccounts() {
        AccountManager accountManager = AccountManager.get(mContext);
        return accountManager.getAccountsByType(mAccountType);
    }
}
