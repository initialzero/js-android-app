package com.jaspersoft.android.jaspermobile.data.validator;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.ValidationRule;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class ProfileReservedValidation implements ValidationRule<Profile, ProfileReservedException> {

    private final String mReservedName;

    @Inject
    public ProfileReservedValidation(@Named("reserved_account_name") String reservedName) {
        mReservedName = reservedName;
    }

    @Override
    public void validate(@NonNull Profile profile) throws ProfileReservedException {
        final String profileName = profile.getKey();
        if (mReservedName.equals(profileName)) {
            throw new ProfileReservedException();
        }
    }
}
