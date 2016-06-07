/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.rx;


import rx.Subscriber;
import rx.functions.Action0;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */

/**
 * Cache subject for 1 subscriber. In case of another subscriber subscribe, previous one will not get anny events.
 */
public final class SingleCacheSubject<T> extends Subject<T, T> {
    private CacheSubscriber<T> mSubjectSubscriber;

    public static <T> SingleCacheSubject<T> create() {
        return new SingleCacheSubject<T>(new CacheSubscriber<T>());
    }

    protected SingleCacheSubject(CacheSubscriber<T> cacheSubscriber) {
        super(cacheSubscriber);
        mSubjectSubscriber = cacheSubscriber;
    }

    @Override
    public boolean hasObservers() {
        return mSubjectSubscriber.hasObservers();
    }

    @Override
    public void onCompleted() {
        mSubjectSubscriber.deliverOnComplete();
    }

    @Override
    public void onError(Throwable e) {
        mSubjectSubscriber.deliverOnError(e);
    }

    @Override
    public void onNext(T t) {
        mSubjectSubscriber.deliverOnNext(t);
    }

    private static class CacheSubscriber<T> implements OnSubscribe<T> {
        private Subscriber<? super T> mSubscriber;
        private T mValue;
        private boolean mHasNext;

        @Override
        public void call(final Subscriber<? super T> subscriber) {
            subscriber.add(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    mSubscriber = null;
                }
            }));

            mSubscriber = subscriber;

            if (mHasNext) {
                subscriber.onNext(mValue);
                mValue = null;
                mHasNext = false;
            }
        }

        public void deliverOnNext(T value) {
            if (hasObservers()) {
                mSubscriber.onNext(value);
            } else {
                mValue = value;
                mHasNext = true;
            }
        }

        public void deliverOnComplete() {
            if (hasObservers()) {
                mSubscriber.onCompleted();
            }
        }

        public void deliverOnError(Throwable error) {
            if (hasObservers()) {
                mSubscriber.onError(error);
            }
        }

        public boolean hasObservers() {
            return mSubscriber != null;
        }
    }
}
