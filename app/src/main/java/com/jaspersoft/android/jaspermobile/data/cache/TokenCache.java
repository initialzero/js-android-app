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

package com.jaspersoft.android.jaspermobile.data.cache;

import com.jaspersoft.android.jaspermobile.domain.Profile;

/**
 * Abstraction around token cache
 * <br/>
 * Implemented by {@link TokenCacheImpl}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface TokenCache {
    /**
     * Retrieves toke from cache on the basis of profile
     *
     * @param profile associated with token
     * @return latest cached token
     */
    String get(Profile profile);

    /**
     * Saves token on disk
     *
     * @param profile associated with token
     * @param token we are going to save
     */
    void put(Profile profile, String token);

    /**
     * Checks weather token exists in cache
     *
     * @param profile associated with token
     * @return flags weather token cached or not
     */
    boolean isCached(Profile profile);
}
