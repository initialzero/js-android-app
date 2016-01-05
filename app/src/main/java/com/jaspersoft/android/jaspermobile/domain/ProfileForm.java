package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileForm {
    private final String mAlias;
    private final String mServerUrl;
    private final AppCredentials mCredentials;

    private ProfileForm(@NonNull String alias, @NonNull String serverUrl, @NonNull AppCredentials credentials) {
        mAlias = alias;
        mServerUrl = serverUrl;
        mCredentials = credentials;
    }

    @NonNull
    public Profile getProfile() {
        return Profile.create(mAlias);
    }

    @NonNull
    public String getServerUrl() {
        return mServerUrl;
    }

    @NonNull
    public AppCredentials getCredentials() {
        return mCredentials;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileForm)) return false;

        ProfileForm form = (ProfileForm) o;

        if (mAlias != null ? !mAlias.equals(form.mAlias) : form.mAlias != null) return false;
        if (mServerUrl != null ? !mServerUrl.equals(form.mServerUrl) : form.mServerUrl != null)
            return false;
        return mCredentials != null ? mCredentials.equals(form.mCredentials) : form.mCredentials == null;
    }

    @Override
    public final int hashCode() {
        int result = mAlias != null ? mAlias.hashCode() : 0;
        result = 31 * result + (mServerUrl != null ? mServerUrl.hashCode() : 0);
        result = 31 * result + (mCredentials != null ? mCredentials.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileModel{" +
                "mAlias='" + mAlias + '\'' +
                ", mServerUrl='" + mServerUrl + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String mAlias;
        private String mBaseUrl;
        private AppCredentials mCredentials;

        private Builder() {
        }

        public Builder setAlias(String alias) {
            mAlias = alias;
            return this;
        }

        public Builder setBaseUrl(String url) {
            mBaseUrl = url;
            return this;
        }

        public Builder setCredentials(AppCredentials credentials) {
            mCredentials = credentials;
            return this;
        }

        public ProfileForm build() {
            return new ProfileForm(mAlias, mBaseUrl, mCredentials);
        }
    }
}
