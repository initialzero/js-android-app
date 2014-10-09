package com.jaspersoft.android.jaspermobile.test.utils;

import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class RequestExecutionAssertion {
    private final Object response;

    public RequestExecutionAssertion(Object response) {
        this.response = response;
    }

    public <T> T getResponse() { return (T) response; }

    public abstract <T> void assertExecution(SpiceRequest<T> request, RequestListener<T> requestListener);
}
