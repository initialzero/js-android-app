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

package com.jaspersoft.android.jaspermobile.util.server;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public interface ServerInfoProvider {
    /**
     * Returns current selected server version. For instance it could be 5.5
     *
     * @return null if app is in consistent state otherwise current value
     */
    @Nullable
    String getServerVersion();
    /**
     * Returns current selected server version. For instance it could be CE or PRO
     *
     * @return null if app is in consistent state otherwise current value
     */
    @Nullable
    String getServerEdition();
    /**
     * Returns current selected server organization. For instance it could be 'organization_1'
     *
     * @return empty string if user has not supplied organization
     */
    @NonNull
    String getOrganization();
    /**
     * This property identifies user on server side. Never changes and can be only deleted.
     *
     * @return user name identifier.
     */
    @NonNull
    String getUsername();
    /**
     * This property internal app identifier. For instance that could be name of account
     *
     * @return internal app identifier.
     */
    @NonNull
    String getAlias();
}
