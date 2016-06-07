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

package com.jaspersoft.android.jaspermobile.ui.model.visualize;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class VisualizeExecOptions {
    @NonNull
    private final String mUri;
    @NonNull
    private final String mParams;
    @NonNull
    private final AppCredentials mAppCredentials;
    private final double mDiagonal;

    public VisualizeExecOptions(@NonNull String uri,
                                @NonNull String params,
                                @NonNull AppCredentials appCredentials,
                                double diagonal) {
        mUri = uri;
        mParams = params;
        mAppCredentials = appCredentials;
        mDiagonal = diagonal;
    }

    @NonNull
    public String getParams() {
        return mParams;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }

    @NonNull
    public AppCredentials getAppCredentials() {
        return mAppCredentials;
    }

    public double getDiagonal() {
        return mDiagonal;
    }

    public static class Builder {
        private String mUri;
        private String mParams;
        private AppCredentials mAppCredentials;
        private double mDiagonal;

        public Builder setUri(String uri) {
            mUri = uri;
            return this;
        }

        public Builder setParams(String params) {
            mParams = params;
            return this;
        }

        public Builder setAppCredentials(AppCredentials appCredentials) {
            mAppCredentials = appCredentials;
            return this;
        }

        public Builder setDiagonal(double diagonal) {
            mDiagonal = diagonal;
            return this;
        }

        public VisualizeExecOptions build() {
            return new VisualizeExecOptions(mUri, mParams, mAppCredentials, mDiagonal);
        }
    }
}
