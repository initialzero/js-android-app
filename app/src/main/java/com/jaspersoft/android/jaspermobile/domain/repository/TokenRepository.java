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

package com.jaspersoft.android.jaspermobile.domain.repository;

import com.jaspersoft.android.jaspermobile.data.repository.TokenDataRepository;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;

/**
 * Abstraction of repo pattern for token related CRUD operations.
 * <br/>
 * Implemented by {@link TokenDataRepository}
 * @author Tom Koptel
 * @since 2.3
 */
public interface TokenRepository {
    /**
     * Relieves token for profile on the basis of credentials for corresponding server.
     *
     * @param profile associated with token
     * @param server we are requesting token from
     * @param credentials used to authorize user
     * @return token which is cookie received from Jasper Server
     * @throws RestStatusException describes either network exception, http exception or Jasper Server specific error states
     */
    String getToken(Profile profile, JasperServer server, BaseCredentials credentials) throws RestStatusException;
}
