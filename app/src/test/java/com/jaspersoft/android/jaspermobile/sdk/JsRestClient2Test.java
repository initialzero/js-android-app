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

package com.jaspersoft.android.jaspermobile.sdk;

import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;
import com.jaspersoft.android.retrofit.sdk.rest.JsRestClient2;
import com.jaspersoft.android.retrofit.sdk.rest.service.AccountService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import retrofit.MockRestAdapter;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.Header;
import rx.Observable;
import rx.schedulers.ImmediateScheduler;
import rx.schedulers.Schedulers;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class JsRestClient2Test {
    private JsRestClient2 restClient;
    private MockRestAdapter mockRestAdapter;
    private RestAdapter restAdapter;


    private static class MockAccountService implements AccountService {
        @Override
        public Observable<Response> authorize(@Header("Authorization") String authToken, @Header("Accept-Language") String locale) {
            return Observable.error(new RuntimeException("Exception from MockAccountService#authorize"));
        }

        @Override
        public Observable<ServerInfo> getServerInfo(@Header("Set-cookie") String cookie) {
            return Observable.error(new RuntimeException("Exception from MockAccountService#getServerInfo"));
        }
    }

    @Before
    public void setUp() {
        restAdapter = new RestAdapter.Builder() //
                .setEndpoint("http://example.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        mockRestAdapter = MockRestAdapter.from(restAdapter);
        mockRestAdapter.setDelay(2000);

        restClient = spy(JsRestClient2.builder()
                .setRestAdapter(restAdapter)
                .build());
    }

    @Test
    public void testAuthorizeMethodThSowsError() {
        ImmediateScheduler immediateScheduler = (ImmediateScheduler) Schedulers.immediate();
        AccountService mockService = mockRestAdapter.create(AccountService.class, new MockAccountService());
        doReturn(mockService).when(restClient).getAccountService();
        restClient.login("any", "any", "any");
    }
}
