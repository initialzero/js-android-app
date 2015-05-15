package com.jaspersoft.android.jaspermobile.cookie;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface JsCookieManager {
    Observable<Boolean> manage();
}
