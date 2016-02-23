package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileMetadata {
    private static final String MOBILE_DEMO_LABEL = "Mobile Demo";

    @NonNull
    private final Profile mProfile;
    @NonNull
    private final JasperServer mServer;
    private final boolean mActive;

    public ProfileMetadata(
            @NonNull Profile profile,
            @NonNull JasperServer server,
            boolean active
    ) {
        mProfile = profile;
        mServer = server;
        mActive = active;
    }

    public boolean isDemo() {
        return MOBILE_DEMO_LABEL.equals(mProfile.getKey());
    }

    public boolean isActive() {
        return mActive;
    }

    @NonNull
    public Profile getProfile() {
        return mProfile;
    }

    @NonNull
    public JasperServer getServer() {
        return mServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileMetadata that = (ProfileMetadata) o;

        if (mActive != that.mActive) return false;
        if (mProfile != null ? !mProfile.equals(that.mProfile) : that.mProfile != null)
            return false;
        return mServer != null ? mServer.equals(that.mServer) : that.mServer == null;

    }

    @Override
    public int hashCode() {
        int result = mProfile != null ? mProfile.hashCode() : 0;
        result = 31 * result + (mServer != null ? mServer.hashCode() : 0);
        result = 31 * result + (mActive ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileMetadata{" +
                "active=" + mActive +
                ", profile=" + mProfile +
                ", server=" + mServer +
                '}';
    }
}
