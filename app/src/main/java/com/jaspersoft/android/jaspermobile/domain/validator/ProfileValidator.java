package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.data.validator.ProfileValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;

/**
 * Abstraction around profile validation
 * <br/>
 * Implemented by {@link ProfileValidatorImpl}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface ProfileValidator {
    /**
     * Validates weather profile unique or does not takes reserved name
     *
     * @param profile we are performing validations on
     * @throws DuplicateProfileException flags out that profile with corresponding alias already exist
     * @throws ProfileReservedException flags out that user passed reserved profile name
     */
    void validate(Profile profile) throws DuplicateProfileException, ProfileReservedException;
}
