/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.print;

import android.support.annotation.NonNull;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class ResourceProviderDecorator implements ObservableResourceProvider {

    private final FileResourceProvider fileResourceProvider;

    private ResourceProviderDecorator(FileResourceProvider fileResourceProvider) {
        this.fileResourceProvider = fileResourceProvider;
    }

    public static ObservableResourceProvider decorate(FileResourceProvider fileResourceProvider) {
        return new ResourceProviderDecorator(fileResourceProvider);
    }

    @Override
    public Observable<File> provideResource() {
        return Observable.create(getSubscriptionTask())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    private Observable.OnSubscribe<File> getSubscriptionTask() {
        return new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                try {
                    File pdfExport = fileResourceProvider.provideResource();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(pdfExport);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        };
    }

}
