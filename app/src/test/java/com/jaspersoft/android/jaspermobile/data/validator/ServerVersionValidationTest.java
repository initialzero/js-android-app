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

package com.jaspersoft.android.jaspermobile.data.validator;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.JasperServerMapper;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.fail;


/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ServerVersionValidationTest {

    @Mock
    JasperServerMapper serverMapper;
    private ServerVersionValidation validator;
    private JasperServer.Builder mServerBuilder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        validator = new ServerVersionValidation();
        mServerBuilder = new JasperServer.Builder()
                .setEdition("PRO")
                .setBaseUrl("http://localhost");
    }

    @Test
    public void serverThatIsEquals5_5IsValid() throws Exception {
        JasperServer server = mServerBuilder
                .setVersion(ServerVersion.v5_5.toString())
                .create();
        validator.validate(server);
    }

    @Test
    public void serverThatIsHigherThan5_5IsValid() throws Exception {
        JasperServer server = mServerBuilder
                .setVersion(ServerVersion.v6.toString())
                .create();
        validator.validate(server);
    }

    @Test
    public void serverThatIsEquals5_0IsNotValid() throws Exception {
        JasperServer server = mServerBuilder
                .setVersion("5.0")
                .create();
        try {
            validator.validate(server);
            fail("Should throw ServerVersionNotSupportedException");
        } catch (Exception ex) {
        }
    }
}