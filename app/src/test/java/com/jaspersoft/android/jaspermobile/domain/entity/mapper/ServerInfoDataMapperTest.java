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

package com.jaspersoft.android.jaspermobile.domain.entity.mapper;

import com.jaspersoft.android.jaspermobile.data.server.JasperServer;
import com.jaspersoft.android.sdk.service.data.server.ServerEdition;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerInfo.class})
public class ServerInfoDataMapperTest {
    @Mock
    ServerInfo mServerInfo;
    ServerInfoDataMapper mapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mapper = new ServerInfoDataMapper();
    }

    @Test
    public void testTransform() throws Exception {
        when(mServerInfo.getEdition()).thenReturn(ServerEdition.CE);
        when(mServerInfo.getVersion()).thenReturn(ServerVersion.AMBER);

        JasperServer server = mapper.transform("http://localhost", mServerInfo);
        assertThat(server.getEdition(), is("CE"));
        assertThat(server.getVersion(), is(6.0d));
        assertThat(server.getBaseUrl(), is("http://localhost"));
    }
}