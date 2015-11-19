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

package com.jaspersoft.android.jaspermobile.data.network;

import com.jaspersoft.android.jaspermobile.domain.network.RestErrorCodes;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(JUnitParamsRunner.class)
public class RestStatusExceptionMapperTest {

    private RestStatusExceptionMapper mapper;

    @Before
    public void setup() {
        mapper = new RestStatusExceptionMapper();
    }

    @Test
    @Parameters(method = "sampleCodes")
    public void testTransform(int sdkCode, int appCode) throws Exception {
        ServiceException statusException = new ServiceException("Message", new IOException(), sdkCode);
        RestStatusException restEx = mapper.transform(statusException);
        assertThat(restEx.code(), is(appCode));
        assertThat(restEx.getMessage(), is("Message"));
        assertThat(restEx.getCause(), is(instanceOf(IOException.class)));
    }

    private Object[] sampleCodes() {
        return new Object[]{
                new Object[] {StatusCodes.UNDEFINED_ERROR,             RestErrorCodes.UNDEFINED_ERROR},
                new Object[] {StatusCodes.NETWORK_ERROR,               RestErrorCodes.NETWORK_ERROR},
                new Object[] {StatusCodes.CLIENT_ERROR,                RestErrorCodes.CLIENT_ERROR},
                new Object[] {StatusCodes.INTERNAL_ERROR,              RestErrorCodes.INTERNAL_ERROR},
                new Object[] {StatusCodes.PERMISSION_DENIED_ERROR,     RestErrorCodes.PERMISSION_DENIED_ERROR},
                new Object[] {StatusCodes.AUTHORIZATION_ERROR,         RestErrorCodes.AUTHORIZATION_ERROR},
                new Object[] {StatusCodes.EXPORT_PAGE_OUT_OF_RANGE,    RestErrorCodes.EXPORT_PAGE_OUT_OF_RANGE},
                new Object[] {StatusCodes.EXPORT_EXECUTION_CANCELLED,  RestErrorCodes.EXPORT_EXECUTION_CANCELLED},
                new Object[] {StatusCodes.EXPORT_EXECUTION_FAILED,     RestErrorCodes.EXPORT_EXECUTION_FAILED},
                new Object[] {StatusCodes.REPORT_EXECUTION_CANCELLED,  RestErrorCodes.REPORT_EXECUTION_CANCELLED},
                new Object[] {StatusCodes.REPORT_EXECUTION_FAILED,     RestErrorCodes.REPORT_EXECUTION_FAILED},
        };
    }
}