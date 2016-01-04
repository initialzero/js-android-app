package com.jaspersoft.android.jaspermobile.domain.interactor;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface UseCase<Result, Argument> {
    void execute(Argument argument, Subscriber<? super Result> subscriber);
    void unsubscribe();
}
