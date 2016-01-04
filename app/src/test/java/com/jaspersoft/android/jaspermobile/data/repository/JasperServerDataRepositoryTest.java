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

import com.jaspersoft.android.jaspermobile.data.cache.ServerCache;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.ServerValidator;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class JasperServerDataRepositoryTest {

    public static final String SERVER_URL = "http://localhost";
    @Mock
    ServerCache mServerCache;
    @Mock
    ServerValidator mServerValidator;

    JasperServer server5_5, server6_0, serverCE, serverPRO;
    JasperServerDataRepository repoUnderTest;
    Profile fakeProfile;
    JasperServer fakeServer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        JasperServer.Builder serverBuilder = JasperServer.builder().setBaseUrl(SERVER_URL);
        server5_5 = serverCE = serverBuilder.setVersion(ServerVersion.v5_5).setEditionIsPro(false).create();
        server6_0 = serverPRO = serverBuilder.setVersion(ServerVersion.v6).setEditionIsPro(true).create();

        repoUnderTest = new JasperServerDataRepository(mServerCache, mServerValidator);
        fakeProfile = Profile.create("name");
        fakeServer = JasperServer.builder()
                .setBaseUrl("http://localhost")
                .setVersion(ServerVersion.v6)
                .setEditionIsPro(true)
                .create();
    }

    @Test
    public void testSaveServer() throws Exception {
        when(mServerValidator.validate(anyString())).thenReturn(Observable.just(server5_5));

        TestSubscriber<Profile> test = new TestSubscriber<>();

        repoUnderTest.saveServer(fakeProfile, SERVER_URL).subscribe(test);

        verify(mServerValidator).validate(SERVER_URL);
        verify(mServerCache).put(fakeProfile, server5_5);
    }

    @Ignore
    public void testGetServer() throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Ignore
    public void testUpdateServer() throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Ignore
    public void testUpdateServerIfVersionUpdated() throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Ignore
    public void testUpdateServerIfEditionUpdated() throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Ignore
    public void testShouldNotUpdateServerIfInstancesEqual() throws Exception {
       throw new UnsupportedOperationException("Not yet implemented");
    }
}