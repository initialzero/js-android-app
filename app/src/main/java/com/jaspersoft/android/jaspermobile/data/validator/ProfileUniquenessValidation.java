package com.jaspersoft.android.jaspermobile.data.validator;

import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.ValidationRule;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class ProfileUniquenessValidation implements ValidationRule<Profile, DuplicateProfileException> {
    private final ProfileCache mProfileCache;

    @Inject
    public ProfileUniquenessValidation(ProfileCache profileCache) {
        mProfileCache = profileCache;
    }

    @Override
    public void validate(Profile profile) throws DuplicateProfileException {
        if (mProfileCache.hasProfile(profile)) {
            throw new DuplicateProfileException(profile.getKey());
        }
    }
}
