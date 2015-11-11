package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ServerVersionNotSupportedException;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ServerValidationFactory {
    Validation<ServerVersionNotSupportedException> create(JasperServer server);
}
