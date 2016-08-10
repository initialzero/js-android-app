/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.sdk.network.SpringCredentials;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class TestCredentialsMapper {

    private CredentialsMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new CredentialsMapper();
    }

    @Test
    public void testToNetworkModel() throws Exception {
        AppCredentials credentials = AppCredentials.builder()
                .setOrganization("organization")
                .setUsername("user")
                .setPassword("1234")
                .create();

        SpringCredentials result = (SpringCredentials) mapper.toNetworkModel(credentials);
        assertThat("Failed to map organization", result.getOrganization(), is("organization"));
        assertThat("Failed to map username",result.getUsername(), is("user"));
        assertThat("Failed to map password",result.getPassword(), is("1234"));
    }
}