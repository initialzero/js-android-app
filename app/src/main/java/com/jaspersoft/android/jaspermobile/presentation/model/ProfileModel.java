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

package com.jaspersoft.android.jaspermobile.presentation.model;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileModel {
    private final String mAlias;
    private final String mBaseUrl;
    private final CredentialsModel mCredentials;

    private ProfileModel(@NonNull String alias, @NonNull String baseUrl, @NonNull CredentialsModel credentials) {
        mAlias = alias;
        mBaseUrl = baseUrl;
        mCredentials = credentials;
    }

    @NonNull
    public String getAlias() {
        return mAlias;
    }

    @NonNull
    public String getBaseUrl() {
        return mBaseUrl;
    }

    @NonNull
    public CredentialsModel getCredentials() {
        return mCredentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileModel that = (ProfileModel) o;

        if (mAlias != null ? !mAlias.equals(that.mAlias) : that.mAlias != null) return false;
        if (mBaseUrl != null ? !mBaseUrl.equals(that.mBaseUrl) : that.mBaseUrl != null)
            return false;
        return !(mCredentials != null ? !mCredentials.equals(that.mCredentials) : that.mCredentials != null);
    }

    @Override
    public int hashCode() {
        int result = mAlias != null ? mAlias.hashCode() : 0;
        result = 31 * result + (mBaseUrl != null ? mBaseUrl.hashCode() : 0);
        result = 31 * result + (mCredentials != null ? mCredentials.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileModel{" +
                "mAlias='" + mAlias + '\'' +
                ", mBaseUrl='" + mBaseUrl + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String mAlias;
        private String mBaseUrl;
        private CredentialsModel mCredentials;

        private Builder() {
        }

        public Builder setAlias(String alias) {
            mAlias = alias;
            return this;
        }

        public Builder setBaseUrl(String baseUrl) {
            mBaseUrl = baseUrl;
            return this;
        }

        public Builder setCredentials(CredentialsModel credentials) {
            mCredentials = credentials;
            return this;
        }

        public ProfileModel create() {
            return new ProfileModel(mAlias, mBaseUrl, mCredentials);
        }
    }
}
