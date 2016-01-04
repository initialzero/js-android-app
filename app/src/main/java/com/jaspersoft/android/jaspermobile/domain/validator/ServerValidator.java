package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.data.validator.ServerValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;

import rx.Observable;

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
     */
    Observable<JasperServer> validate(String serverUrl);
}
