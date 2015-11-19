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

package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.data.repository.datasource.TokenDataSource;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class TokenDataRepositoryTest {

    @Mock
    JasperServer mJasperServer;
    @Mock
    BaseCredentials mBaseCredentials;
    @Mock
    Profile mProfile;

    @Mock
    TokenDataSource.Factory mFactory;
    @Mock
    TokenDataSource mDataSource;

    TokenDataRepository repository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        repository = new TokenDataRepository(mFactory);
        when(mFactory.create(any(Profile.class), any(JasperServer.class), any(BaseCredentials.class))).thenReturn(mDataSource);
    }

    @Test
    public void testGetTokenFromCache() throws Exception {
        repository.getToken(mProfile, mJasperServer, mBaseCredentials);
        verify(mFactory).create(mProfile, mJasperServer, mBaseCredentials);
        verify(mDataSource).retrieveToken();
    }
}