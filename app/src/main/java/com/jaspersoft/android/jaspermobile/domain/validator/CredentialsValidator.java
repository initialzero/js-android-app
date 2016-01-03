package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.data.validator.CredentialsValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;

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
     * @param server instance of Jasper server we are going to use during validation process
     * @param credentials user data we use to
     * @throws RestStatusException describes either network exception, http exception or Jasper Server specific error states
     */
    void validate(JasperServer server, BaseCredentials credentials) throws RestStatusException;
}
