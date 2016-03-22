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

package com.jaspersoft.android.jaspermobile.data.cache.profile;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.Profile;

/**
 * Abstraction around active profile cache
 * <br/>
 * Following interface implemented by {@link PreferencesActiveProfileCache}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface ActiveProfileCache {
    /**
     * Retrieves active profile from cache
     *
     * @return profile if exists in cache otherwise null
     */
    @Nullable
    Profile get();

    /**
     * Saves target profile in cache
     *
     * @param profile target profile
     */
    Profile put(@NonNull Profile profile);

    /**
     * Checks weather profile exists in cache
     * @return true if exists
     */
    boolean hasProfile();

    /**
     * Clears data from preference cache
     */
    void clear();
}
