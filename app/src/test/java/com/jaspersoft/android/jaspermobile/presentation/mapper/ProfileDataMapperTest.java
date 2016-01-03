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

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.presentation.model.CredentialsModel;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileDataMapperTest {
    @Mock
    CredentialsModel mCredentialsModel;

    ProfileModel profileModel;
    ProfileDataMapper mapper;

    @Before
    public void setUp() throws Exception {
        profileModel = ProfileModel.builder()
                .setAlias("alias")
                .setBaseUrl("http://localhost")
                .setCredentials(mCredentialsModel)
                .create();
        mapper = new ProfileDataMapper();
    }

    @Test
    public void testTransform() throws Exception {
        Profile domainProfile = mapper.transform(profileModel);
        assertThat(domainProfile.getKey(), is(profileModel.getAlias()));
    }
}