package com.jaspersoft.android.jaspermobile.widget;

import android.os.Looper;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class PaginationBarViewPagesOnSubscribe implements Observable.OnSubscribe<Integer> {
    private final AbstractPaginationView mPaginationBarView;

    public PaginationBarViewPagesOnSubscribe(AbstractPaginationView paginationBarView) {
        mPaginationBarView = paginationBarView;
    }

    @Override
    public void call(final Subscriber<? super Integer> subscriber) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException(
                    "Must be called from the main thread. Was: " + Thread.currentThread());
        }

        mPaginationBarView.setOnPageChangeListener(new AbstractPaginationView.OnPageChangeListener() {
            @Override
            public void onPageSelected(int currentPage) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(currentPage);
                }
            }
        });

        subscriber.add(new MainThreadSubscription() {
            @Override protected void onUnsubscribe() {
                mPaginationBarView.setOnPageChangeListener(null);
            }
        });
    }
}
