package com.jaspersoft.android.jaspermobile.network;

import android.content.Context;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class SimpleRequestListener2<T> implements RequestListener<T> {

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        RequestExceptionHandler2.handle(spiceException, getContext());
    }

    protected abstract Context getContext();
}
