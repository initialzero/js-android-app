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
public final class VisualizeTemplate {
    @NonNull
    private final String mContent;
    @NonNull
    private final String mServerUrl;

    public VisualizeTemplate(@NonNull String content, @NonNull String serverUrl) {
        mContent = content;
        mServerUrl = serverUrl;
    }

    @NonNull
    public String getContent() {
        return mContent;
    }

    @NonNull
    public String getServerUrl() {
        return mServerUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisualizeTemplate that = (VisualizeTemplate) o;

        if (!mContent.equals(that.mContent)) return false;
        return mServerUrl.equals(that.mServerUrl);
    }

    @Override
    public int hashCode() {
        int result = mContent.hashCode();
        result = 31 * result + mServerUrl.hashCode();
        return result;
    }
}
