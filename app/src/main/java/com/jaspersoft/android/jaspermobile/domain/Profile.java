/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class Profile {
    private final String key;

    private Profile(String key) {
        this.key = key;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    @NonNull
    public static Profile create(String key){
        return new Profile(key);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;

        Profile profile = (Profile) o;

        return !(key != null ? !key.equals(profile.key) : profile.key != null);
    }

    @Override
    public final int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "key='" + key + '\'' +
                '}';
    }
}
