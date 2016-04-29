package com.jaspersoft.android.jaspermobile.webview;

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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
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
    }

    @Test
    public void lollipop_intercept_api_should_delegate_to_interceptor() throws Exception {
        givenNativeRequestMapper();
        givenNativeResponseMapper();
        givenRequestInterceptor();

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

        whenLegacyApiInterceptsRequest();

        thenShouldMapUrlToGenericRequest();
        thenShouldInterceptRequest();
        thenShouldMapGenericResponseToNative();
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

    private void givenNativeResponseMapper() {
        when(nativeWebResponseMapper.toNativeResponse(any(WebResponse.class))).thenReturn(webResourceResponse);
    }

    private void givenNativeRequestMapper() {
        when(nativeWebRequestMapper.toGenericRequest(any(WebResourceRequest.class))).thenReturn(webRequest);
        when(nativeWebRequestMapper.toGenericRequest(anyString())).thenReturn(webRequest);
    }
}