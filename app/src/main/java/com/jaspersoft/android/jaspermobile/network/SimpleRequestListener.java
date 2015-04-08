package com.jaspersoft.android.jaspermobile.network;

import android.content.Context;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class SimpleRequestListener<T> implements RequestListener<T> {

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        RequestExceptionHandler.handle(spiceException, getContext());
    }

    protected abstract Context getContext();
}
