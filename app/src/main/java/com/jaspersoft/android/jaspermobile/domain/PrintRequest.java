package com.jaspersoft.android.jaspermobile.domain;

import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class PrintRequest {
    @NonNull
    private final PageRequest mPageRequest;
    @NonNull
    private final ParcelFileDescriptor mDestination;

    public PrintRequest(@NonNull PageRequest pageRequest,
                        @NonNull ParcelFileDescriptor destination) {
        mPageRequest = pageRequest;
        mDestination = destination;
    }

    @NonNull
    public ParcelFileDescriptor getDestination() {
        return mDestination;
    }

    @NonNull
    public PageRequest getPageRequest() {
        return mPageRequest;
    }
}
