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

package com.jaspersoft.android.jaspermobile.presentation.mapper;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.presentation.model.CredentialsModel;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class CredentialsDataMapperTest {

    private CredentialsModel credentialsModel;
    private CredentialsDataMapper mapper;

    @Before
    public void setUp() throws Exception {
        credentialsModel = CredentialsModel.builder()
                .setUsername("user")
                .setPassword("1234")
                .setOrganization("organization")
                .create();
        mapper = new CredentialsDataMapper();
    }

    @Test
    public void testTransform() throws Exception {
        AppCredentials domainCredentials = mapper.transform(credentialsModel);
        assertThat(domainCredentials.getUsername(), is(credentialsModel.getUsername()));
        assertThat(domainCredentials.getPassword(), is(credentialsModel.getPassword()));
        assertThat(domainCredentials.getOrganization(), is(credentialsModel.getOrganization()));
    }
}