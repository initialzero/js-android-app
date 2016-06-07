/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

/**
 * SDK constants
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperSettings {
    // Intent actions
    public static final String ACTION_AUTHORIZE = "jaspersoft.intent.action.AUTHORIZE";
    public static final String ACTION_TOKEN_EXPIRED = "jaspersoft.intent.action.TOKEN_EXPIRED";
    public static final String ACTION_INVALID_PASSWORD = "jaspersoft.intent.action.INVALID_PASSWORD";
    public static final String ACTION_REST_ERROR = "jaspersoft.intent.action.REST_ERROR";

    // Auth constants
    public static final String RESERVED_ACCOUNT_NAME = "com.jaspersoft.account.none";
    public static final String JASPER_ACCOUNT_TYPE = "com.jaspersoft";
    public static final String JASPER_AUTH_TOKEN_TYPE = "FULL ACCESS";

    // REST constants
    public static final String DEFAULT_REST_VERSION = "/rest_v2";

    private JasperSettings() {
        throw new RuntimeException();
    }

}
