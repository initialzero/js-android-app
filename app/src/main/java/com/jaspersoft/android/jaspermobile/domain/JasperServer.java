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
import android.support.annotation.Nullable;

import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class JasperServer {
    private final String baseUrl;
    private final Boolean edition;
    private final ServerVersion version;

    private JasperServer(String baseUrl, Boolean edition, ServerVersion version) {
        this.baseUrl = baseUrl;
        this.edition = edition;
        this.version = version;
    }

    @NonNull
    public String getBaseUrl() {
        return baseUrl;
    }

    @Nullable
    public Boolean isProEdition() {
        return edition;
    }

    @Nullable
    public String getVersionName() {
        if (version == null) {
            return null;
        }
        return version.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JasperServer)) return false;

        JasperServer that = (JasperServer) o;

        if (baseUrl != null ? !baseUrl.equals(that.baseUrl) : that.baseUrl != null) return false;
        if (edition != null ? !edition.equals(that.edition) : that.edition != null) return false;
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public final int hashCode() {
        int result = baseUrl != null ? baseUrl.hashCode() : 0;
        result = 31 * result + (edition != null ? edition.hashCode() : 0);
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
        private Boolean mEdition;
        private ServerVersion mVersion;

        private Builder() {
        }

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
