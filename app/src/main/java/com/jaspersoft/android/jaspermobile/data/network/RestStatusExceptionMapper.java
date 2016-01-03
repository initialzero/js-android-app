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

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Adapts SDK specific exception application one
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class RestStatusExceptionMapper {
    @Inject
    public RestStatusExceptionMapper() {
    }

    public RestStatusException transform(ServiceException statusException) {
        return new RestStatusException(
                statusException.getMessage(),
                statusException.getCause(),
                adaptCodes(statusException.code())
        );
    }

    private int adaptCodes(int sdkCode) {
        switch (sdkCode) {
            case RestErrorCodes.UNDEFINED_ERROR:
                return StatusCodes.UNDEFINED_ERROR;
            case RestErrorCodes.NETWORK_ERROR:
                return StatusCodes.NETWORK_ERROR;
            case RestErrorCodes.CLIENT_ERROR:
                return StatusCodes.CLIENT_ERROR;
            case RestErrorCodes.INTERNAL_ERROR:
                return StatusCodes.INTERNAL_ERROR;
            case RestErrorCodes.PERMISSION_DENIED_ERROR:
                return StatusCodes.PERMISSION_DENIED_ERROR;
            case RestErrorCodes.AUTHORIZATION_ERROR:
                return StatusCodes.AUTHORIZATION_ERROR;
            case RestErrorCodes.EXPORT_PAGE_OUT_OF_RANGE:
                return StatusCodes.EXPORT_PAGE_OUT_OF_RANGE;
            case RestErrorCodes.EXPORT_EXECUTION_CANCELLED:
                return StatusCodes.EXPORT_EXECUTION_CANCELLED;
            case RestErrorCodes.EXPORT_EXECUTION_FAILED:
                return StatusCodes.EXPORT_EXECUTION_FAILED;
            case RestErrorCodes.REPORT_EXECUTION_CANCELLED:
                return StatusCodes.REPORT_EXECUTION_CANCELLED;
            case RestErrorCodes.REPORT_EXECUTION_FAILED:
                return StatusCodes.REPORT_EXECUTION_FAILED;
            default:
                return RestErrorCodes.UNDEFINED_ERROR;
        }
    }
}
