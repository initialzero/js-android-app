/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.springframework.http.client.ClientHttpResponse;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StreamResourceProviderDecoratorTest {

    @Mock
    ResourceProvider<ClientHttpResponse> streamResourceProvider;
    @Mock
    ClientHttpResponse clientHttpResponse;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldProvideFile() {
        when(streamResourceProvider.provideResource()).thenReturn(clientHttpResponse);

        ResourceProvider<Observable<ClientHttpResponse>> resourceProvider = StreamResourceProviderDecorator.decorate(streamResourceProvider);
        Observable<ClientHttpResponse> observable = resourceProvider.provideResource();

        observable.subscribeOn(AndroidSchedulers.mainThread()).subscribeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Action1<ClientHttpResponse>() {
            @Override
            public void call(ClientHttpResponse response) {
                assertThat(response, is(notNullValue()));
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void shouldHandleError() {
        when(streamResourceProvider.provideResource()).thenThrow(new IllegalStateException());

        ResourceProvider<Observable<ClientHttpResponse>> resourceProvider = StreamResourceProviderDecorator.decorate(streamResourceProvider);
        resourceProvider
                .provideResource()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ClientHttpResponse>() {
                    @Override
                    public void call(ClientHttpResponse response) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                });
    }
}
