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

package com.jaspersoft.android.jaspermobile.data.server;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class JasperServer {
    private final String baseUrl;
    private final String edition;
    private final double version;

    private JasperServer(String baseUrl, String edition, double version) {
        this.baseUrl = baseUrl;
        this.edition = edition;
        this.version = version;
    }

    @NonNull
    public String getBaseUrl() {
        return baseUrl;
    }

    @NonNull
    public String getEdition() {
        return edition;
    }

    public double getVersion() {
        return version;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JasperServer that = (JasperServer) o;

        if (Double.compare(that.version, version) != 0) return false;
        if (baseUrl != null ? !baseUrl.equals(that.baseUrl) : that.baseUrl != null) return false;
        return !(edition != null ? !edition.equals(that.edition) : that.edition != null);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = baseUrl != null ? baseUrl.hashCode() : 0;
        result = 31 * result + (edition != null ? edition.hashCode() : 0);
        temp = Double.doubleToLongBits(version);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JasperServer{" +
                "baseUrl='" + baseUrl + '\'' +
                ", edition='" + edition + '\'' +
                ", version=" + version +
                '}';
    }

    public static class Builder {
        private String mBaseUrl;
        private String mEdition;
        private double mVersion;

        private Builder() {}

        public Builder setBaseUrl(String baseUrl) {
            mBaseUrl = baseUrl;
            return this;
        }

        public Builder setEdition(String edition) {
            mEdition = edition;
            return this;
        }

        public Builder setVersion(double version) {
            mVersion = version;
            return this;
        }

        public JasperServer create() {
            return new JasperServer(mBaseUrl, mEdition, mVersion);
        }
    }
}
