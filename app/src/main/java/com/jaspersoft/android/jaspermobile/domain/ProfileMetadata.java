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

package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileMetadata {
    private static final String MOBILE_DEMO_LABEL = "Mobile Demo";

    @NonNull
    private final Profile mProfile;
    @NonNull
    private final JasperServer mServer;
    private final boolean mActive;

    public ProfileMetadata(
            @NonNull Profile profile,
            @NonNull JasperServer server,
            boolean active
    ) {
        mProfile = profile;
        mServer = server;
        mActive = active;
    }

    public boolean isDemo() {
        return MOBILE_DEMO_LABEL.equals(mProfile.getKey());
    }

    public boolean isActive() {
        return mActive;
    }

    @NonNull
    public Profile getProfile() {
        return mProfile;
    }

    @NonNull
    public JasperServer getServer() {
        return mServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileMetadata that = (ProfileMetadata) o;

        if (mActive != that.mActive) return false;
        if (mProfile != null ? !mProfile.equals(that.mProfile) : that.mProfile != null)
            return false;
        return mServer != null ? mServer.equals(that.mServer) : that.mServer == null;

    }

    @Override
    public int hashCode() {
        int result = mProfile != null ? mProfile.hashCode() : 0;
        result = 31 * result + (mServer != null ? mServer.hashCode() : 0);
        result = 31 * result + (mActive ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileMetadata{" +
                "active=" + mActive +
                ", profile=" + mProfile +
                ", server=" + mServer +
                '}';
    }
}
