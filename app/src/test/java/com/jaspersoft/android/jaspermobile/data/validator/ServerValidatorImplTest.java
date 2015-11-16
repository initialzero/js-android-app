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

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.validator.ServerValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static junit.framework.Assert.fail;


/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ServerValidatorImplTest {
    public static final String MINIMUM_SUPPORTED_VERSION_MESSAGE = "Server with version with 5.5 is a minimum we support!";
    ServerValidator validator;

    @Rule
    public ExpectedException mException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        validator = new ServerValidatorImpl();
    }

    @Test
    public void serverThatIsEquals5_5IsValid() {
        try {
            validator.validate(JasperServer.builder().setVersion(5.5d).create());
        } catch (ServerVersionNotSupportedException e) {
            fail(MINIMUM_SUPPORTED_VERSION_MESSAGE);
        }
    }

    @Test
    public void serverThatIsHigherThan5_5IsValid() throws Exception {
        try {
            validator.validate(JasperServer.builder().setVersion(6.0d).create());
        } catch (ServerVersionNotSupportedException e) {
            fail(MINIMUM_SUPPORTED_VERSION_MESSAGE);
        }
    }

    @Test
    public void serverThatIsEquals5_0IsNotValid() throws Exception {
        mException.expect(ServerVersionNotSupportedException.class);
        mException.expectMessage("Version of server should be 5.5 of higher, but was: 5.0");
        validator.validate(JasperServer.builder().setVersion(5.0d).create());
    }
}