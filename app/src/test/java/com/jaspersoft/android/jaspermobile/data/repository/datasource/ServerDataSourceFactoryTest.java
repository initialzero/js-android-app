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

package com.jaspersoft.android.jaspermobile.data.repository.datasource;

import com.jaspersoft.android.jaspermobile.data.cache.JasperServerCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ServerDataSourceFactoryTest {
    @Mock
    ServerApi.Factory mServerApiFactory;
    @Mock
    JasperServerCache mServerCache;

    private ServerDataSource.Factory dataSourceFactory;
    private final Profile fakeProfile = Profile.create("alias");

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        dataSourceFactory = new ServerDataSource.Factory(mServerApiFactory, mServerCache);
    }

    @Test
    public void testCreateCloudDataSource() throws Exception {
        ServerDataSource dataSource = dataSourceFactory.createCloudDataSource();
        assertThat(dataSource, is(instanceOf(CloudServerDataSource.class)));
    }

    @Test
    public void testCreateCloudSourceIfCacheMissing() throws Exception {
        when(mServerCache.hasServer(fakeProfile)).thenReturn(false);

        ServerDataSource dataSource = dataSourceFactory.createDataSource(fakeProfile);
        assertThat(dataSource, is(instanceOf(CloudServerDataSource.class)));
    }

    @Test
    public void testCreateDiskSourceIfCacheExists() throws Exception {
        when(mServerCache.hasServer(fakeProfile)).thenReturn(true);

        ServerDataSource dataSource = dataSourceFactory.createDataSource(fakeProfile);
        assertThat(dataSource, is(instanceOf(DiskServerDataSource.class)));
    }

    @Test
    public void testCreateDiskDataSource() throws Exception {
        ServerDataSource dataSource = dataSourceFactory.createDiskDataSource();
        assertThat(dataSource, is(instanceOf(DiskServerDataSource.class)));
    }
}
