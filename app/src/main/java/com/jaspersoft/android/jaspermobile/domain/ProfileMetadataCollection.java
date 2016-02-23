package com.jaspersoft.android.jaspermobile.domain;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileMetadataCollection {
    private final List<ProfileMetadata> mProfiles;
    private final boolean mContainsActiveProfile;

    public ProfileMetadataCollection(List<ProfileMetadata> profiles) {
        mProfiles = profiles;
        mContainsActiveProfile = searchForActiveProfile(profiles);
    }

    private boolean searchForActiveProfile(List<ProfileMetadata> profiles) {
        for (ProfileMetadata profile : profiles) {
            if (profile.isActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsActiveProfile() {
        return mContainsActiveProfile;
    }

    public List<ProfileMetadata> get() {
        return mProfiles;
    }
}
