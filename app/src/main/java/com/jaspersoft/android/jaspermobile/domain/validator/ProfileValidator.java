package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ProfileValidator {
    void validate(Profile profile) throws DuplicateProfileException, ProfileReservedException;
}
