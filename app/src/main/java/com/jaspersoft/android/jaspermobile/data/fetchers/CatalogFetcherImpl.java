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

package com.jaspersoft.android.jaspermobile.data.fetchers;

import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.entity.Resource;
import com.jaspersoft.android.jaspermobile.domain.fetchers.CatalogFetcher;
import com.jaspersoft.android.jaspermobile.util.rx.RxTransformer;
import com.jaspersoft.android.sdk.service.exception.ServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
public abstract class CatalogFetcherImpl<SearchType, ResourceType extends Resource> implements CatalogFetcher {

    private List<Resource> mResourceList;
    private LoaderCallback mLoaderCallback;
    private Subscription mSearchSubscription;
    private CompositeSubscription mSubscriptionsList;
    private boolean mPreviousWasEmpty;
    private boolean mDelivered;

    public CatalogFetcherImpl() {
        this.mSubscriptionsList = new CompositeSubscription();
        this.mResourceList = new ArrayList<>();
        this.mLoaderCallback = EMPTY;
        this.mPreviousWasEmpty = true;
        this.mDelivered = true;
    }

    public LoaderCallback getLoaderCallback() {
        return mLoaderCallback;
    }

    public List<Resource> getResourceList() {
        return mResourceList;
    }

    @Override
    public void subscribe(LoaderCallback loaderCallback) {
        mLoaderCallback = loaderCallback;

        if (!mDelivered) {
            mLoaderCallback.onLoaded(mResourceList);
            mDelivered = true;
        }
    }

    @Override
    public void unsubscribe() {
        mLoaderCallback = EMPTY;
    }

    @Override
    public void reset() {
        if (mSearchSubscription != null) {
            mSearchSubscription.unsubscribe();
            mSearchSubscription = null;
        }
        mResourceList = new ArrayList<>();
        search();
    }

    @Override
    public void search() {
        if (!searchTaskInitialized()) {
            createSearchTask();
            searchNext();
            return;
        }
        if (hasNext()) {
            searchNext();
        }
    }

    private void searchNext() {
        if (mSearchSubscription != null) return;

        int delay = mPreviousWasEmpty ? 0 : 750;

        mSearchSubscription = getNextTask()
                .delay(delay, TimeUnit.MILLISECONDS)
                .compose(RxTransformer.<List<SearchType>>applySchedulers())
                .subscribe(new SimpleSubscriber<List<SearchType>>() {
                    @Override
                    public void onStart() {
                        mLoaderCallback.onLoadStarted(mResourceList.isEmpty());
                    }

                    @Override
                    public void onNext(List<SearchType> items) {
                        mPreviousWasEmpty = items.isEmpty();
                        mResourceList.addAll(map(items));
                        mLoaderCallback.onLoaded(mResourceList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mPreviousWasEmpty = true;
                        mLoaderCallback.onError((ServiceException) e, mResourceList.isEmpty());
                    }

                    @Override
                    public void onCompleted() {
                        mSearchSubscription = null;
                    }
                });
    }

    protected void observe(Observable<Void> observable) {
        mSubscriptionsList.add(observable.subscribe(new ResourcesObserver()));
    }

    protected abstract boolean searchTaskInitialized();

    protected abstract void createSearchTask();

    protected abstract boolean hasNext();

    protected abstract Observable<List<SearchType>> getNextTask();

    protected abstract List<ResourceType> map(List<SearchType> items);

    protected class ResourcesObserver extends SimpleSubscriber<Void> {
        @Override
        public void onNext(Void item) {
            reset();
        }
    }

    LoaderCallback EMPTY = new LoaderCallback() {
        @Override
        public void onLoadStarted(boolean first) {

        }

        @Override
        public void onLoaded(List<Resource> resources) {
            mDelivered = false;
        }

        @Override
        public void onError(ServiceException ex, boolean first) {

        }
    };
}
