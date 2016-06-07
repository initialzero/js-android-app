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
import android.support.annotation.Nullable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class AppCredentials {
    public static final String NO_PASSWORD = "none";

    private final String username;
    private final String password;
    private final String organization;

    public AppCredentials(String username, String password, String organization) {
        this.username = username;
        this.password = password;
        this.organization = organization;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    @Nullable
    public String getOrganization() {
        return organization;
    }

    public Builder newBuilder() {
        return new Builder()
                .setUsername(username)
                .setPassword(password)
                .setOrganization(organization);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppCredentials)) return false;

        AppCredentials that = (AppCredentials) o;

        if (username != null ? !username.equals(that.username) : that.username != null)
            return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        return !(organization != null ? !organization.equals(that.organization) : that.organization != null);
    }

    @Override
    public final int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BaseCredentials{" +
                "organization='" + organization + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    public static class Builder {
        private String mUsername;
        private String mPassword;
        private String mOrganization;

        private Builder() {}

        public Builder setUsername(String username) {
            mUsername = username;
            return this;
        }

        public Builder setPassword(String password) {
            mPassword = password;
            return this;
        }

        public Builder setOrganization(String organization) {
            mOrganization = organization;
            return this;
        }

        public AppCredentials create() {
            if (mPassword == null) {
                mPassword = NO_PASSWORD;
            }
            return new AppCredentials(mUsername, mPassword, mOrganization);
        }
    }
}
