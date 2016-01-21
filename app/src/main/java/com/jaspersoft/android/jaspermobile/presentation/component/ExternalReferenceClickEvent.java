package com.jaspersoft.android.jaspermobile.presentation.component;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ExternalReferenceClickEvent {
    @NonNull
    private final String mExternalReference;

    public ExternalReferenceClickEvent(@NonNull String externalReference) {
        mExternalReference = externalReference;
    }

    @NonNull
    public String getExternalReference() {
        return mExternalReference;
    }
}
