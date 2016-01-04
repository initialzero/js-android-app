package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.data.validator.CredentialsValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;

import rx.Observable;

/**
 * Abstraction around credentials validation
 * <br/>
 * Implemented by {@link CredentialsValidatorImpl}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface CredentialsValidator {
    /**
     * Checks credentials on server side
     *
     * @param credentials user data we use to
     */
    Observable<AppCredentials> validate(AppCredentials credentials);
}
