/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.retrofit.sdk.token;

import android.text.TextUtils;
import android.util.Base64;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class BasicAccessTokenEncoder implements AccessTokenEncoder {
    private final String mOrganization;
    private final String mUsername;
    private final String mPassword;

    public static Builder builder() {
        return new Builder();
    }

    private BasicAccessTokenEncoder(String mOrganization, String mUsername, String mPassword) {
        this.mOrganization = mOrganization;
        this.mUsername = mUsername;
        this.mPassword = mPassword;
    }

    @Override
    public String encodeToken() {
        String mergedName = TextUtils.isEmpty(mOrganization)
                ? mUsername : (mUsername + "|" + mOrganization);
        String salt = String.format("%s:%s", mergedName, mPassword);
        return "Basic " + Base64.encodeToString(salt.getBytes(), Base64.NO_WRAP);
    }

    public static class Builder {
        private String mOrganization;
        private String mUsername;
        private String mPassword;

        public Builder setOrganization(String organization) {
            this.mOrganization = organization;
            return this;
        }

        public Builder setUsername(String username) {
            if (TextUtils.isEmpty(username)) {
                throw new IllegalArgumentException("Username should not be empty");
            }
            this.mUsername = username;
            return this;
        }

        public Builder setPassword(String password) {
            if (TextUtils.isEmpty(password)) {
                throw new IllegalArgumentException("Password should not be empty");
            }
            this.mPassword = password;
            return this;
        }

        public BasicAccessTokenEncoder build() {
            return new BasicAccessTokenEncoder(mOrganization, mUsername, mPassword);
        }
    }
}
