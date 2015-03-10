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

package com.jaspersoft.android.jaspermobile.sdk;

import android.accounts.Account;

import com.jaspersoft.android.jaspermobile.test.support.UnitTestSpecification;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountProvider;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class BasicAccountProviderTest extends UnitTestSpecification {
    private JasperAccountProvider storage;

    @Before
    public void setUp() {
        storage = JasperAccountProvider.get(getContext());
    }

    @Test
    public void testBasicMethods() {
        storage.putAccount(new Account("cookie", "any"));
        Account account = storage.getAccount();
        assertThat(account, notNullValue());
        assertThat(account.name, is("cookie"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactoryShouldNotAcceptNullContext() {
        JasperAccountProvider.get(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetMethodShouldFollowContract() {
        JasperAccountProvider.get(getContext()).getAccount();
    }
}
