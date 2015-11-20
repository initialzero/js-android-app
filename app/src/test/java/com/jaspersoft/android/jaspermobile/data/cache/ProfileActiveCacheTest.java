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

import android.app.Activity;
import android.content.SharedPreferences;

import com.jaspersoft.android.jaspermobile.domain.Profile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ProfileActiveCacheTest {

    private PreferencesProfileCache cacheUnderTest;
    private Profile fakeProfile;
    private SharedPreferences store;

    @Before
    public void setUp() throws Exception {
        cacheUnderTest = new PreferencesProfileCache(RuntimeEnvironment.application);
        store = RuntimeEnvironment.application.getSharedPreferences("JasperAccountManager", Activity.MODE_PRIVATE);
        fakeProfile = Profile.create("name");

        assertThat("Precondition failed. Store should be empty",
                store.getAll().isEmpty()
        );
    }

    @Test
    public void testPut() throws Exception {
        cacheUnderTest.put(fakeProfile);
        assertThat("Failed to put " + fakeProfile + " in cache",
                !store.getAll().isEmpty()
        );
    }

    @Test
    public void testGet() throws Exception {
        store.edit().putString("ACCOUNT_NAME_KEY", "name").apply();
        assertThat("Failed to retrieve " + fakeProfile + " back",
                cacheUnderTest.get().equals(fakeProfile)
        );
    }

    @Test
    public void testEvict() throws Exception {
        store.edit().putString("ACCOUNT_NAME_KEY", "name").apply();
        cacheUnderTest.evict();
        assertThat("Failed to evict profile",
                store.getAll().isEmpty()
        );
    }
}