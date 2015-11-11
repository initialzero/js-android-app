package com.jaspersoft.android.jaspermobile.domain.validator;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface Validation<E extends Exception> {
    E getCheckedException();
    boolean perform();
}
