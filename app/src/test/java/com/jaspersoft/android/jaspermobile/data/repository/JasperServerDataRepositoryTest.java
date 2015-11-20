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

import com.jaspersoft.android.jaspermobile.data.repository.datasource.ServerDataSource;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class JasperServerDataRepositoryTest {

    @Mock
    ServerDataSource.Factory mDataSourceFactory;
    @Mock
    ServerDataSource mCloudDataSource;
    @Mock
    ServerDataSource mDiskDataSource;

    JasperServer server5_5, server6_0, serverCE, serverPRO;
    JasperServerDataRepository repoUnderTest;
    Profile fakeProfile;
    JasperServer fakeServer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        JasperServer.Builder serverBuilder = JasperServer.builder().setBaseUrl("http://localhost");
        server5_5 = serverCE = serverBuilder.setVersion(5.5d).setEdition("CE").create();
        server6_0 = serverPRO = serverBuilder.setVersion(6.0d).setEdition("PRO").create();

        when(mDataSourceFactory.createCloudDataSource()).thenReturn(mCloudDataSource);
        when(mDataSourceFactory.createDiskDataSource()).thenReturn(mDiskDataSource);
        when(mDataSourceFactory.createDataSource(any(Profile.class))).thenReturn(mDiskDataSource);

        when(mDiskDataSource.getServer(any(Profile.class))).thenReturn(server5_5);
        when(mCloudDataSource.getServer(any(Profile.class))).thenReturn(server5_5);

        repoUnderTest = new JasperServerDataRepository(mDataSourceFactory);
        fakeProfile = Profile.create("name");
        fakeServer = JasperServer.builder()
                .setBaseUrl("http://localhost")
                .setVersion(6.0d)
                .setEdition("PRO")
                .create();
    }

    @Test
    public void testSaveServer() throws Exception {
        repoUnderTest.saveServer(fakeProfile, fakeServer);
        verify(mDataSourceFactory).createDiskDataSource();
        verify(mDiskDataSource).saveServer(fakeProfile, fakeServer);
    }

    @Test
    public void testGetServer() throws Exception {
        repoUnderTest.getServer(fakeProfile);
        verify(mDataSourceFactory).createDataSource(fakeProfile);
        verify(mDiskDataSource).getServer(fakeProfile);
    }

    @Test
    public void testUpdateServer() throws Exception {
        repoUnderTest.updateServer(fakeProfile);
        verify(mDataSourceFactory).createDiskDataSource();
        verify(mDataSourceFactory).createCloudDataSource();
        verify(mCloudDataSource).getServer(fakeProfile);
        verify(mDiskDataSource).getServer(fakeProfile);
    }

    @Test
    public void testUpdateServerIfVersionUpdated() throws Exception {
        when(mDiskDataSource.getServer(any(Profile.class))).thenReturn(server5_5);
        when(mCloudDataSource.getServer(any(Profile.class))).thenReturn(server6_0);

        assertThat("Server should be updated while versions differ",
                repoUnderTest.updateServer(fakeProfile)
        );
        verify(mDiskDataSource).saveServer(fakeProfile, server6_0);
    }

    @Test
    public void testUpdateServerIfEditionUpdated() throws Exception {
        when(mDiskDataSource.getServer(any(Profile.class))).thenReturn(serverCE);
        when(mCloudDataSource.getServer(any(Profile.class))).thenReturn(serverPRO);

        assertThat("Server should be updated while editions differ",
                repoUnderTest.updateServer(fakeProfile)
        );
        verify(mDiskDataSource).saveServer(fakeProfile, serverPRO);
    }

    @Test
    public void testShouldNotUpdateServerIfInstancesEqual() throws Exception {
        when(mDiskDataSource.getServer(any(Profile.class))).thenReturn(server5_5);
        when(mCloudDataSource.getServer(any(Profile.class))).thenReturn(server5_5);

        assertThat("Server should not be updated",
                !repoUnderTest.updateServer(fakeProfile)
        );
    }
}