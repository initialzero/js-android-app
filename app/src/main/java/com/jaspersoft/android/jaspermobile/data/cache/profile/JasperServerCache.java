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

package com.jaspersoft.android.jaspermobile.data.cache.profile;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;

/**
 * Abstraction around server cache.
 * Following interface implemented by {@link AccountJasperServerCache}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface JasperServerCache {
    /**
     * Saves server in cache by associating it with {@link Profile}
     *
     * @param profile the target profile we use to associate with server
     * @param jasperServer metadata for server we are going to put in cache
     */
    void put(Profile profile, JasperServer jasperServer);

    /**
     * Retrieves server metadata from cache on the basis of associated {@link Profile}
     * @param profile the target profile we use to associate with server
     * @return {@link JasperServer} abstraction that encompass additional server metadata
     */
    JasperServer get(Profile profile);

    /**
     * Checks weather server meta data was stored on the basis of associated {@link Profile}
     *
     * @param profile the target profile we use to associate with server
     * @return True if cache retains server, False if not
     */
    boolean hasServer(Profile profile);
}
