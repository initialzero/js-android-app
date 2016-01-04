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
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Before;
import org.junit.Test;

import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ServerValidatorImplTest {
    public static final String MINIMUM_SUPPORTED_VERSION_MESSAGE = "Server with version with 5.5 is a minimum we support!";
    ServerValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new ServerValidatorImpl();
    }

    @Test
    public void serverThatIsEquals5_5IsValid() {
        JasperServer server = JasperServer.builder().setVersion(ServerVersion.v5_5).create();
        TestSubscriber<Void> test = new TestSubscriber<>();

        validator.validate(server).subscribe(test);
        test.assertNoErrors();
    }

    @Test
    public void serverThatIsHigherThan5_5IsValid() throws Exception {
        JasperServer server = JasperServer.builder().setVersion(ServerVersion.v6).create();
        TestSubscriber<Void> test = new TestSubscriber<>();

        validator.validate(server).subscribe(test);
        test.assertNoErrors();
    }

    @Test
    public void serverThatIsEquals5_0IsNotValid() throws Exception {
        JasperServer server = JasperServer.builder().setVersion(ServerVersion.valueOf("5.0")).create();
        TestSubscriber<Void> test = new TestSubscriber<>();

        validator.validate(server).subscribe(test);

        ServerVersionNotSupportedException ex = (ServerVersionNotSupportedException) test.getOnErrorEvents().get(0);
        assertThat(MINIMUM_SUPPORTED_VERSION_MESSAGE, ex, is(notNullValue()));
    }
}