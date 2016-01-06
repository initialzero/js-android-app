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

package com.jaspersoft.android.jaspermobile.domain.repository.profile;

import com.jaspersoft.android.jaspermobile.data.repository.profile.JasperServerDataRepository;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;

import rx.Observable;

/**
 * Abstraction responsible for create, update, get, fetch operations around server meta data.
 * Following interface implemented by {@link JasperServerDataRepository}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface JasperServerRepository {
    /**
     * Saves server
     *
     * @param profile the target profile we use to associate with credentials
     * @param baseUrl the http url that points to users Jasper server
     */
    Observable<Profile> saveServer(final Profile profile, final String baseUrl);

    /**
     * Retrieves server instance from cache.
     *
     * @param profile the target profile we use to associate with credentials
     * @return {@link JasperServer} abstraction that encompass additional server metadata
     */
    Observable<JasperServer> getServer(Profile profile);
}
