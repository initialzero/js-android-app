package com.jaspersoft.android.jaspermobile.presentation.component;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface WebViewEvents {
    Observable<WebViewErrorEvent> receivedErrorEvent();
    Observable<Integer> progressChangedEvent();
    Observable<Void> sessionExpiredEvent();
}
