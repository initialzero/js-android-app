package com.jaspersoft.android.jaspermobile.domain.validator;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ValidationRule<Target, Exception extends Throwable> {
    void validate(@NonNull Target target) throws Exception;
}
