package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.data.validator.ProfileValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.Profile;

import rx.Observable;

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
     */
    Observable<Profile> validate(Profile profile);
}
