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

package com.jaspersoft.android.jaspermobile.sdk;

import android.util.Base64;

import com.jaspersoft.android.retrofit.sdk.token.BasicAccessTokenEncoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BasicAccessTokenEncoderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderShouldNotAcceptNullUsername() {
        BasicAccessTokenEncoder.builder()
                .setUsername(null)
                .setPassword("my_password")
                .setOrganization(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderShouldNotAcceptNullPassword() {
        BasicAccessTokenEncoder.builder()
                .setUsername("username")
                .setPassword(null)
                .setOrganization(null)
                .build();
    }

    @Test
    public void testInstanceShouldReturnEncodedValue() {
        BasicAccessTokenEncoder encoder = BasicAccessTokenEncoder.builder()
                .setUsername("username")
                .setPassword("1234")
                .setOrganization(null)
                .build();
        String token = encoder.encodeToken();

        assertThat(token, notNullValue());
    }

    @Test
    public void testBasicImplementationConsumesBase64() {
        BasicAccessTokenEncoder encoder = BasicAccessTokenEncoder.builder()
                .setUsername("username")
                .setPassword("1234")
                .setOrganization(null)
                .build();
        String token = encoder.encodeToken();

        assertThat(token, containsString("Basic "));

        String hash = token.split(" ")[1];
        String rawString = new String(Base64.decode(hash, Base64.NO_WRAP));
        assertThat(rawString, is("username:1234"));
    }

    @Test
    public void testEncodesOrganizationValueAsWell() {
        BasicAccessTokenEncoder encoder = BasicAccessTokenEncoder.builder()
                .setUsername("username")
                .setPassword("1234")
                .setOrganization("organization")
                .build();

        String token = encoder.encodeToken();

        assertThat(token, containsString("Basic "));

        String hash = token.split(" ")[1];
        String rawString = new String(Base64.decode(hash, Base64.NO_WRAP));
        assertThat(rawString, is("username|organization:1234"));
    }

}
