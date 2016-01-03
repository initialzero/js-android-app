package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.data.validator.ServerValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;

/**
 * Abstraction around server validation
 * <br/>
 * Implemented by {@link ServerValidatorImpl}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface ServerValidator {
    /**
     * Validates either server version supported or not.
     *
     * @param server we are perform validation on
     * @throws ServerVersionNotSupportedException if server version is lower than 5.5
     */
    void validate(JasperServer server) throws ServerVersionNotSupportedException;
}
