package com.jaspersoft.android.jaspermobile.domain;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class SimpleSubscriber<R> extends Subscriber<R> {
    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
    }

    @Override
    public void onNext(R item) {
    }
}
