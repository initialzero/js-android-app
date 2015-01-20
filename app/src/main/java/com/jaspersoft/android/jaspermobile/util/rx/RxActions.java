package com.jaspersoft.android.jaspermobile.util.rx;

import rx.functions.Action1;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class RxActions {
    public static Action1<Throwable> createLogErrorAction(String tag) {
        Timber.tag(tag);
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Timber.e(throwable, "Failed to load subscriptions");
            }
        };
    }
}
