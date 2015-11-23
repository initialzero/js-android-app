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

package com.jaspersoft.android.jaspermobile.data.repository.datasource;

import com.jaspersoft.android.jaspermobile.data.cache.JasperServerCache;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;

/**
 * This implementation delegates work to corresponding implementation of {@link JasperServerCache}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public final class DiskServerDataSource implements ServerDataSource {
    private final JasperServerCache mServerCache;

    public DiskServerDataSource(JasperServerCache serverCache) {
        mServerCache = serverCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JasperServer getServer(Profile profile) {
        return mServerCache.get(profile);
    }

    /**
     * There is no implementation that can retrieve server data on the basis server url.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public JasperServer getServer(String baseUrl) throws RestStatusException {
        throw new UnsupportedOperationException("Disk storage has no option for retrieving server data by url");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveServer(Profile profile, JasperServer server) {
        mServerCache.put(profile, server);
    }
}
