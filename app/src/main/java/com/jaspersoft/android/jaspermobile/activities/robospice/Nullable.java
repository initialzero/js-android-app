package com.jaspersoft.android.jaspermobile.activities.robospice;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Nullable annotation specifically kept with RUNTIME retention so that Roboguice can recognize it
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Retention(RUNTIME)
@Target({METHOD, PARAMETER, FIELD})
public @interface Nullable {
}
