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

package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class JasperServer {
    private final String baseUrl;
    private final boolean edition;
    private final ServerVersion version;

    private JasperServer(String baseUrl, boolean edition, ServerVersion version) {
        this.baseUrl = baseUrl;
        this.edition = edition;
        this.version = version;
    }

    @NonNull
    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isProEdition() {
        return edition;
    }

    public String getVersionName() {
        return version.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JasperServer that = (JasperServer) o;

        if (edition != that.edition) return false;
        if (baseUrl != null ? !baseUrl.equals(that.baseUrl) : that.baseUrl != null) return false;
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public int hashCode() {
        int result = baseUrl != null ? baseUrl.hashCode() : 0;
        result = 31 * result + (edition ? 1 : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String editionName = edition ? "PRO" : "CE";
        return "JasperServer{" +
                "baseUrl='" + baseUrl + '\'' +
                ", edition='" + editionName + '\'' +
                ", version=" + version +
                '}';
    }

    public static class Builder {
        private String mBaseUrl;
        private boolean mEdition;
        private ServerVersion mVersion;

        private Builder() {}

        public Builder setBaseUrl(String baseUrl) {
            mBaseUrl = baseUrl;
            return this;
        }

        public Builder setEditionIsPro(boolean edition) {
            mEdition = edition;
            return this;
        }

        public Builder setVersion(ServerVersion version) {
            mVersion = version;
            return this;
        }

        public JasperServer create() {
            return new JasperServer(mBaseUrl, mEdition, mVersion);
        }
    }
}
