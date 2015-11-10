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

package com.jaspersoft.android.jaspermobile.domain.executor;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.data.validator.ProfileValidatorImpl;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ProfileValidatorImplTest {

    private ProfileValidatorImpl validator;

    @Before
    public void setUp() throws Exception {
        validator = new ProfileValidatorImpl(RuntimeEnvironment.application, JasperSettings.JASPER_ACCOUNT_TYPE);
    }

    @Test
    public void shouldRejectProfileIfAccountAlreadyRegistered() throws Exception {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        boolean isAdded = accountManager.addAccountExplicitly(new Account("name", JasperSettings.JASPER_ACCOUNT_TYPE), null, null);
        assertThat("Precondition failed. Could not add test account", isAdded);

        Account[] accounts = accountManager.getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        assertThat("Precondition failed. Test account is missing", accounts.length > 0);

        boolean isValid = validator.validate(Profile.create("name"));
        assertThat("Account should not be valid", !isValid);
    }

    @Test
    public void shouldAcceptProfileIfUnique() {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        Account[] accounts = accountManager.getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        assertThat(Arrays.asList(accounts), is(empty()));

        boolean isValid = validator.validate(Profile.create("name"));
        assertThat("Account should be valid", isValid);
    }
}