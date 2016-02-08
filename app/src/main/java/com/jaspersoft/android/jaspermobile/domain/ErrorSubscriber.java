package com.jaspersoft.android.jaspermobile.domain;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class ErrorSubscriber<R> extends SimpleSubscriber<R> {
    private final SimpleSubscriber<R> mDelegate;

    protected ErrorSubscriber(SimpleSubscriber<R> delegate) {
        mDelegate = delegate;
    }

    @Override
    public void onCompleted() {
        mDelegate.onCompleted();
    }

    @Override
    public void onNext(R item) {
        mDelegate.onNext(item);
    }
}
