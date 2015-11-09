package com.jaspersoft.android.jaspermobile.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class SpringCredentials extends Credentials {
    private final String username;
    private final String password;
    private final String organization;

    public SpringCredentials(String username, String password, String organization) {
        this.username = username;
        this.password = password;
        this.organization = organization;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "SpringCredentials{" +
                "username='" + username + '\'' +
                ", organization='" + organization + '\'' +
                '}';
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpringCredentials that = (SpringCredentials) o;

        if (!username.equals(that.username)) return false;
        if (!password.equals(that.password)) return false;
        return !(organization != null ? !organization.equals(that.organization) : that.organization != null);

    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        return result;
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

        public SpringCredentials create() {
            return new SpringCredentials(mUsername, mPassword, mOrganization);
        }
    }
}
