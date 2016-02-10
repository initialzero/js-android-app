package com.jaspersoft.android.jaspermobile.presentation.model;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ProfileViewModel {
    @NonNull
    private final String mLabel;
    @NonNull
    private final String mVersion;
    private final boolean mIsActive;

    public ProfileViewModel(@NonNull String label, @NonNull String version, boolean isActive) {
        mLabel = label;
        mVersion = version;
        mIsActive = isActive;
    }

    @NonNull
    public String getLabel() {
        return mLabel;
    }

    @NonNull
    public String getVersion() {
        return mVersion;
    }

    public boolean isActive() {
        return mIsActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileViewModel that = (ProfileViewModel) o;

        if (mIsActive != that.mIsActive) return false;
        if (!mLabel.equals(that.mLabel)) return false;
        return mVersion.equals(that.mVersion);

    }

    @Override
    public int hashCode() {
        int result = mLabel.hashCode();
        result = 31 * result + mVersion.hashCode();
        result = 31 * result + (mIsActive ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileViewModel{" +
                "isActive=" + mIsActive +
                ", label='" + mLabel + '\'' +
                ", version=" + mVersion +
                '}';
    }
}
