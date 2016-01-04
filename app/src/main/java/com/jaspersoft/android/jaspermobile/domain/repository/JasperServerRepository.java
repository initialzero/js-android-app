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

import com.jaspersoft.android.jaspermobile.data.repository.JasperServerDataRepository;
import com.jaspersoft.android.jaspermobile.data.repository.datasource.CloudServerDataSource;
import com.jaspersoft.android.jaspermobile.data.repository.datasource.DiskServerDataSource;
import com.jaspersoft.android.jaspermobile.data.repository.datasource.ServerDataSource;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;

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
     * Saves server in corresponding {@link ServerDataSource}.
     * {@link CloudServerDataSource} does not support this operation.
     *
     * @param profile the target profile we use to associate with credentials
     * @param jasperServer the target server we are going to save
     */
    Observable<Void> saveServer(Profile profile, JasperServer jasperServer);

    /**
     * Loads server data from corresponding {@link ServerDataSource}.
     * {@link DiskServerDataSource} does not support this operation.
     *
     * @param baseUrl the http url that points to users Jasper server
     * @return {@link JasperServer} abstraction that encompass additional server metadata
     * @throws RestStatusException describes either network exception, http exception or Jasper Server specific error states
     */
    Observable<JasperServer> loadServer(String baseUrl) throws RestStatusException;

    /**
     * Retrieves server instance from corresponding {@link ServerDataSource}.
     *
     * @param profile the target profile we use to associate with credentials
     * @return {@link JasperServer} abstraction that encompass additional server metadata
     */
    Observable<JasperServer> getServer(Profile profile);

    /**
     * Fetches and updates server data in corresponding {@link ServerDataSource}.
     *
     * @param profile the target profile we use to associate with credentials
     * @return true if server was updated. False if server metadata has not changed, as result was not updated
     */
    Observable<Boolean> updateServer(Profile profile);
}
