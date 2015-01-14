/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.sdk;

import com.jaspersoft.android.jaspermobile.test.support.UnitTestSpecification;
import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;
import com.jaspersoft.android.retrofit.sdk.rest.JsRestClient2;
import com.jaspersoft.android.retrofit.sdk.rest.service.AccountService;

import org.junit.Before;
import org.junit.Test;

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
public class JsRestClient2Test extends UnitTestSpecification {
    private JsRestClient2 restClient;
    private MockRestAdapter mockRestAdapter;
    private RestAdapter restAdapter;


    private static class MockAccountService implements AccountService {
        @Override
        public Observable<Response> authorize(@Header("Authorization") String authToken) {
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

        restClient = spy(JsRestClient2.builder(getContext())
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
