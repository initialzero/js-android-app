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

package com.jaspersoft.android.jaspermobile.webview;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.intercept.WebResourceInterceptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class SystemWebViewClientTest {

    private static final String ANY_URL = "http://localhost";

    @Mock
    NativeWebRequestMapper nativeWebRequestMapper;
    @Mock
    NativeWebResponseMapper nativeWebResponseMapper;
    @Mock
    WebResourceInterceptor requestInterceptor;

    @Mock
    WebRequest webRequest;
    @Mock
    WebResponse webResponse;

    @Mock
    WebView webView;
    @Mock
    WebResourceRequest webResourceRequest;
    @Mock
    WebResourceResponse webResourceResponse;

    private SystemWebViewClient clientUnderTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        clientUnderTest = new SystemWebViewClient.Builder()
                .withRequestMapper(nativeWebRequestMapper)
                .withResponseMapper(nativeWebResponseMapper)
                .registerInterceptor(requestInterceptor)
                .build();

        givenWebResourceRequest();
    }

    @Test
    public void lollipop_intercept_api_should_delegate_to_interceptor() throws Exception {
        givenNativeRequestMapper();
        givenNativeResponseMapper();
        givenRequestInterceptor();
        givenWebResponseWithCode(200);

        whenLollipopApiInterceptsRequest();

        thenShouldMapNativeRequestToGeneric();
        thenShouldInterceptRequest();
        thenShouldMapGenericResponseToNative();
    }

    @Test
    public void legacy_intercept_api_should_delegate_to_interceptor() throws Exception {
        givenNativeRequestMapper();
        givenNativeResponseMapper();
        givenRequestInterceptor();
        givenWebResponseWithCode(200);

        whenLegacyApiInterceptsRequest();

        thenShouldMapUrlToGenericRequest();
        thenShouldInterceptRequest();
        thenShouldMapGenericResponseToNative();
    }

    @Test
    public void lollipop_should_not_intercept_requests_not_in_range_of_200_299() throws Exception {
        givenNativeRequestMapper();
        givenNativeResponseMapper();
        givenRequestInterceptor();
        givenWebResponseWithCode(300);

        whenLollipopApiInterceptsRequest();

        thenShouldNotMapInterceptedRequest();
    }

    @Test
    public void legacy_should_not_intercept_requests_not_in_range_of_200_299() throws Exception {
        givenNativeRequestMapper();
        givenNativeResponseMapper();
        givenRequestInterceptor();
        givenWebResponseWithCode(300);

        whenLegacyApiInterceptsRequest();

        thenShouldNotMapInterceptedRequest();
    }

    private void thenShouldNotMapInterceptedRequest() {
        verifyZeroInteractions(nativeWebResponseMapper);
    }

    private void thenShouldMapUrlToGenericRequest() {
        verify(nativeWebRequestMapper).toGenericRequest(ANY_URL);
    }

    private void thenShouldInterceptRequest() {
        verify(requestInterceptor).interceptRequest(webView, webRequest);
    }

    private void thenShouldMapGenericResponseToNative() {
        verify(nativeWebResponseMapper).toNativeResponse(webResponse);
    }

    private void thenShouldMapNativeRequestToGeneric() {
        verify(nativeWebRequestMapper).toGenericRequest(webResourceRequest);
    }

    private void whenLollipopApiInterceptsRequest() {
        clientUnderTest.shouldInterceptRequest(webView, webResourceRequest);
    }

    private void whenLegacyApiInterceptsRequest() {
        clientUnderTest.shouldInterceptRequest(webView, ANY_URL);
    }

    private void givenRequestInterceptor() {
        when(requestInterceptor.interceptRequest(any(WebView.class), any(WebRequest.class))).thenReturn(webResponse);
    }

    private void givenWebResponseWithCode(int code) {
        when(webResponse.getStatusCode()).thenReturn(code);
    }

    private void givenNativeResponseMapper() {
        when(nativeWebResponseMapper.toNativeResponse(any(WebResponse.class))).thenReturn(webResourceResponse);
    }

    private void givenNativeRequestMapper() {
        when(nativeWebRequestMapper.toGenericRequest(any(WebResourceRequest.class))).thenReturn(webRequest);
        when(nativeWebRequestMapper.toGenericRequest(anyString())).thenReturn(webRequest);
    }

    private void givenWebResourceRequest() {
        when(webResourceRequest.getUrl()).thenReturn(Uri.parse("http://localhost"));
        when(webResourceRequest.getRequestHeaders()).thenReturn(Collections.<String, String>emptyMap());
        when(webResourceRequest.getMethod()).thenReturn("GET");
    }
}