package com.jaspersoft.android.jaspermobile.util.rx;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class DefaultSubscriber<Result> extends Subscriber<Result> {
    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
    }

    @Override
    public void onNext(Result result) {
    }
}
