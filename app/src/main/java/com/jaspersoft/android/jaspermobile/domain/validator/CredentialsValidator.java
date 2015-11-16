package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.InvalidCredentialsException;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface CredentialsValidator {
    void validate(JasperServer server, BaseCredentials credentials) throws InvalidCredentialsException;
}
