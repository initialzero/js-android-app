package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class AppCredentials {
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
            return new AppCredentials(mUsername, mPassword, mOrganization);
        }
    }
}
