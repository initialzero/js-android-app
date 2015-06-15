package com.jaspersoft.android.jaspermobile.util.print;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public interface ResourceProvider<RESULT> {
    @NonNull
    RESULT provideResource();
}
