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

package com.jaspersoft.android.jaspermobile.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.domain.Profile;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class FakeAccount {
    public static final String ACCOUNT_TYPE = "com.jaspersoft";

    private final Profile fakeProfile;
    private final AccountManager accountManager;

    public FakeAccount(Context context, Profile fakeProfile) {
        this.accountManager = AccountManager.get(context);
        this.fakeProfile = fakeProfile;
    }

    public void setup() {
        assertThat("Failed precondition. Can not create account for profile: " + fakeProfile,
                accountManager.addAccountExplicitly(new Account(fakeProfile.getKey(), ACCOUNT_TYPE), null, null)
        );
        assertThat("Fake account should be registered in system",
                accountManager.getAccountsByType(ACCOUNT_TYPE).length > 0
        );
    }

    public Account get() {
        return accountManager.getAccountsByType(ACCOUNT_TYPE)[0];
    }
}
