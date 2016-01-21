package com.jaspersoft.android.jaspermobile.presentation.component;

import android.support.annotation.NonNull;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class RxWebViewEvents implements WebViewEvents {
    @NonNull
    private final WebViewConfiguration mConfiguration;

    RxWebViewEvents(@NonNull WebViewConfiguration configuration) {
        mConfiguration = configuration;
    }

    @Override
    public Observable<WebViewErrorEvent> receivedErrorEvent() {
        return Observable.create(new WebViewErrorReceivedOnSubscribe(mConfiguration));
    }

    @Override
    public Observable<Integer> progressChangedEvent() {
        return Observable.create(new WebViewProgressChangeOnSubscribe(mConfiguration));
    }

    @Override
    public Observable<Void> sessionExpiredEvent() {
        return null;
    }
}
