/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util.security;

import android.provider.Settings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class PasswordManagerTest {

    private PasswordManager passwordManager;

    @Before
    public void setup() {
        passwordManager = PasswordManager.init(RuntimeEnvironment.application, "secret");
        Settings.Secure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ANDROID_ID, "ROBOLECTRICYOUAREBAD");
    }

    @Test
    public void shouldEncryptPassword() {
        String encrypted = passwordManager.encrypt("1234");
        System.out.println(encrypted);
        assertThat(encrypted, is(notNullValue()));
    }

    @Test
    public void shouldDecryptPassword() {
        String encrypted = passwordManager.decrypt("eUu9sU6Ah6c=");
        assertThat(encrypted, is("1234"));
    }
}
