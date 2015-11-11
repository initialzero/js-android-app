package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ProfileValidationFactory {
    Validation<DuplicateProfileException> create(Profile profile);
}
