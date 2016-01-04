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

import android.content.Context;
import android.support.annotation.StringRes;

import com.google.inject.Singleton;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.domain.network.RestErrorCodes;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public class RestErrorAdapter {
    private final Context mContext;

    @Inject
    public RestErrorAdapter(Context context) {
        mContext = context;
    }

    public String transform(RestStatusException statusException) {
        switch (statusException.code()) {
            case RestErrorCodes.NETWORK_ERROR:
                return getString(R.string.no_network);
            case RestErrorCodes.AUTHORIZATION_ERROR:
                return getString(R.string.error_http_401);
            case RestErrorCodes.PERMISSION_DENIED_ERROR:
                return getString(R.string.error_http_403);
            case RestErrorCodes.CLIENT_ERROR:
                return getString(R.string.error_http_404);
            case RestErrorCodes.INTERNAL_ERROR:
                return getString(R.string.error_http_500);
            case RestErrorCodes.EXPORT_PAGE_OUT_OF_RANGE:
                return getString(R.string.rv_out_of_range);
            case RestErrorCodes.REPORT_EXECUTION_CANCELLED:
                return getString(R.string.error_report_cancelled);
            case RestErrorCodes.REPORT_EXECUTION_FAILED:
                return getString(R.string.error_report_failed);
            case RestErrorCodes.EXPORT_EXECUTION_FAILED:
                return getString(R.string.error_export_failed);
            case RestErrorCodes.EXPORT_EXECUTION_CANCELLED:
                return getString(R.string.error_export_cancelled);
            default:
                return getString(R.string.error_undefined);
        }
    }

    private String getString(@StringRes int resId) {
        return mContext.getString(resId);
    }
}
