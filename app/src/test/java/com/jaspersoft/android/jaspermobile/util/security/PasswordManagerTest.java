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

package com.jaspersoft.android.jaspermobile.util.security;

import android.accounts.Account;
import android.provider.Settings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * @author Tom Koptel
 * @since 2.1.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class PasswordManagerTest {

    private PasswordManager passwordManager;
    private Account fakeAccount;

    @Before
    public void setup() {
        Settings.Secure.putString(
                RuntimeEnvironment.application.getContentResolver(),
                Settings.Secure.ANDROID_ID,
                "ROBOLECTRICYOUAREBAD"
        );
        passwordManager = PasswordManager.create(RuntimeEnvironment.application);
        fakeAccount = new Account("test", "com.test");
    }

    @Test
    public void shouldEncryptDecryptPassword() {
        TestSubscriber<Boolean> putSubscriber = new TestSubscriber<>();
        TestSubscriber<String> getSubscriber = new TestSubscriber<>();

        passwordManager.put(fakeAccount, "1234").subscribe(putSubscriber);
        putSubscriber.assertNoErrors();

        passwordManager.get(fakeAccount).subscribe(getSubscriber);
        getSubscriber.assertNoErrors();

        assertThat("Password manager failed to encrypt/decrypt pass", getSubscriber.getOnNextEvents(), hasItem("1234"));
    }
}
