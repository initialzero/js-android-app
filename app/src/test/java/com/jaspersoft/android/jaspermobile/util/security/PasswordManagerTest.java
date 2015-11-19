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
    public void shouldEncryptPassword() throws Exception {
        String encrypted = passwordManager.encrypt("1234");
        assertThat(encrypted, is(notNullValue()));
    }

    @Test
    public void shouldDecryptPassword() throws Exception {
        String decrypted = passwordManager.decrypt("eUu9sU6Ah6c=");
        assertThat(decrypted, is("1234"));
    }

    @Test(expected = PasswordManager.DecryptionError.class)
    public void shouldThrowDecryptionErrorIfErrorEncountered() throws Exception {
        PasswordManager passwordManager2 = PasswordManager.init(RuntimeEnvironment.application, "qwerty123445");
        String encrypted = passwordManager.encrypt("1234");
        passwordManager2.decrypt(encrypted);
    }
}
