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
import com.jaspersoft.android.jaspermobile.domain.validator.ServerValidator;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;
import com.jaspersoft.android.sdk.service.rx.info.RxServerInfoService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;


/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ServerValidatorImplTest {
    public static final String MINIMUM_SUPPORTED_VERSION_MESSAGE = "Server with version with 5.5 is a minimum we support!";

    private static final String SERVER_URL = "http://localhost";

    @Mock
    JasperServerMapper serverMapper;
    @Mock
    RxServerInfoService rxServerInfoService;

    @Mock
    ServerInfo mServerInfo;

    private ServerValidator validator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        validator = new ServerValidatorImpl(serverMapper, rxServerInfoService);
        when(rxServerInfoService.requestServerInfo()).thenReturn(Observable.just(mServerInfo));
    }

    @Test
    public void serverThatIsEquals5_5IsValid() {
        when(mServerInfo.getVersion()).thenReturn(ServerVersion.v5_5);
        TestSubscriber<JasperServer> test = new TestSubscriber<>();

        validator.validate(SERVER_URL).subscribe(test);
        test.assertNoErrors();
    }

    @Test
    public void serverThatIsHigherThan5_5IsValid() throws Exception {
        when(mServerInfo.getVersion()).thenReturn(ServerVersion.v6);
        TestSubscriber<JasperServer> test = new TestSubscriber<>();

        validator.validate(SERVER_URL).subscribe(test);
        test.assertNoErrors();
    }

    @Test
    public void serverThatIsEquals5_0IsNotValid() throws Exception {
        when(mServerInfo.getVersion()).thenReturn(ServerVersion.valueOf("5.0"));
        TestSubscriber<JasperServer> test = new TestSubscriber<>();

        validator.validate(SERVER_URL).subscribe(test);

        ServerVersionNotSupportedException ex = (ServerVersionNotSupportedException) test.getOnErrorEvents().get(0);
        assertThat(MINIMUM_SUPPORTED_VERSION_MESSAGE, ex, is(notNullValue()));
    }
}