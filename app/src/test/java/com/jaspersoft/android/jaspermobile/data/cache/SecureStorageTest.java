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

import android.app.Application;
import android.provider.Settings;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.data.FakeAccountDataMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.account.AccountStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Tom Koptel
 * @since 2.1.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class SecureStorageTest {

    private SecureStorage mSecureStorage;
    private Profile fakeProfile = Profile.create("fake");

    @Before
    public void setup() {
        Application application = RuntimeEnvironment.application;
        Settings.Secure.putString(
                application.getContentResolver(),
                Settings.Secure.ANDROID_ID,
                "ROBOLECTRICYOUAREBAD"
        );

        AccountDataMapper dataMapper = FakeAccountDataMapper.get();
        mSecureStorage = new SecureStorage(
                application,
                dataMapper,
                FakePreExecutionThread.create(),
                FakePostExecutionThread.create()
        );
    }

    @Test
    public void shouldEncryptDecryptPassword() {
        mSecureStorage.put(fakeProfile, AccountStorage.KEY, "1234");
        String pass = mSecureStorage.get(fakeProfile, AccountStorage.KEY);
        assertThat("Password manager failed to encrypt/decrypt pass", pass, is("1234"));
    }
}
