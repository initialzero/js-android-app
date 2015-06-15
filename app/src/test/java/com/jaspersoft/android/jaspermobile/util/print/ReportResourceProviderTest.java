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

import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.Collection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ReportResourceProviderTest {

    @Mock
    JsRestClient jsRestClient;
    @Mock
    Collection<ReportParameter> reportParameters;
    @Mock
    ResourceLookup resourceLookup;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateProviderWithNULLParams() {
        ReportResourceProvider
                .builder(RuntimeEnvironment.application)
                .setJsRestClient(jsRestClient)
                .setResource(resourceLookup)
                .addReportParameters(null)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreateProviderWithoutResourceLookup() {
        ReportResourceProvider
                .builder(RuntimeEnvironment.application)
                .setJsRestClient(jsRestClient)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreateProviderWithoutJsRestClient() {
        ReportResourceProvider
                .builder(RuntimeEnvironment.application)
                .setResource(resourceLookup)
                .build();
    }

    @Test
    public void shouldProvideResource() {
        ResourceProvider resourceProvider = ReportResourceProvider
                .builder(RuntimeEnvironment.application)
                .setJsRestClient(jsRestClient)
                .setResource(resourceLookup)
                .build();

        Observable<File> observable = resourceProvider
                .provideResource();

        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread());

        observable
                .subscribe(
                        new Action1<File>() {
                            @Override
                            public void call(File file) {
                                assertThat(file, is(notNullValue()));
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throw new RuntimeException(throwable);
                            }
                        });
    }
}
