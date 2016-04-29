package com.jaspersoft.android.jaspermobile.webview.intercept.okhttp;

import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;
import com.jaspersoft.android.jaspermobile.webview.WebResponse;
import com.jaspersoft.android.jaspermobile.webview.intercept.WebResourceInterceptor;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class OkHttpWebResourceInterceptorTest {
    private static final String ANY_URL = "http://localhost";

    @Mock
    OkHttpClient okClient;
    @Mock
    Call okCall;
    Request okRequest;
    Response okResponse;

    @Mock
    OkRequestMapper okRequestMapper;
    @Mock
    OkResponseMapper okResponseMapper;
    @Mock
    WebRequest webRequest;
    @Mock
    WebResponse webResponse;

    @Mock
    WebResourceInterceptor.Rule rule;

    @Mock
    WebView webView;

    private OkHttpWebResourceInterceptor interceptor = new OkHttpWebResourceInterceptor();
    private WebResponse mappedResonse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_intercept_request_for_positive_assertion_of_rule() throws Exception {
        givenOkHttpClient();
        givenHappyCaseOkRequestMapper();
        givenOkResponseMapper();
        givenPositiveInterceptRule();
        givenOkWebResourceInterceptor();

        whenInterceptsRequest();

        assertThat(mappedResonse, is(notNullValue()));

        verify(rule).shouldIntercept(webRequest);
        verify(okRequestMapper).toOkHttpRequest(webRequest);
        verify(okClient).newCall(okRequest);
        verify(okCall).execute();
        verify(okResponseMapper).toWebViewResponse(okResponse);
    }

    @Test
    public void should_not_intercept_request_for_negative_assertion_of_rule() throws Exception {
        givenOkHttpClient();
        givenHappyCaseOkRequestMapper();
        givenOkResponseMapper();
        givenNegativeInterceptRule();
        givenOkWebResourceInterceptor();

        whenInterceptsRequest();

        assertThat(mappedResonse, is(nullValue()));

        verify(rule).shouldIntercept(webRequest);
        verifyZeroInteractions(okRequestMapper);
        verifyZeroInteractions(okClient);
        verifyZeroInteractions(okCall);
        verifyZeroInteractions(okResponseMapper);
    }

    @Test
    public void should_not_intercept_request_if_resource_request_mapping_failed() throws Exception {
        givenOkHttpClient();
        givenNullCaseOkRequestMapper();
        givenOkResponseMapper();
        givenPositiveInterceptRule();
        givenOkWebResourceInterceptor();

        whenInterceptsRequest();

        assertThat(mappedResonse, is(nullValue()));

        verify(rule).shouldIntercept(webRequest);
        verify(okRequestMapper).toOkHttpRequest(webRequest);
        verifyZeroInteractions(okClient);
        verifyZeroInteractions(okCall);
        verifyZeroInteractions(okResponseMapper);
    }

    private void whenInterceptsRequest() {
        mappedResonse = interceptor.interceptRequest(webView, webRequest);
    }

    private void givenOkHttpClient() throws Exception {
        okRequest = new Request.Builder()
                .url(ANY_URL)
                .build();
        okResponse = new Response.Builder()
                .protocol(Protocol.HTTP_1_1)
                .request(okRequest)
                .code(200)
                .build();
        when(okCall.execute()).thenReturn(okResponse);
        when(okClient.newCall(any(Request.class))).thenReturn(okCall);
    }

    private void givenPositiveInterceptRule() {
        when(rule.shouldIntercept(any(WebRequest.class))).thenReturn(true);
    }

    private void givenNegativeInterceptRule() {
        when(rule.shouldIntercept(any(WebRequest.class))).thenReturn(false);
    }

    private void givenOkWebResourceInterceptor() throws Exception {
        interceptor = new OkHttpWebResourceInterceptor.Builder()
                .withClient(okClient)
                .registerRule(rule)
                .withRequestMapper(okRequestMapper)
                .withResponseMapper(okResponseMapper)
                .build();
    }

    private void givenNullCaseOkRequestMapper() {
        when(okRequestMapper.toOkHttpRequest(any(WebRequest.class))).thenReturn(null);
    }

    private void givenHappyCaseOkRequestMapper() {
        when(okRequestMapper.toOkHttpRequest(any(WebRequest.class))).thenReturn(okRequest);
    }

    private void givenOkResponseMapper() {
        when(okResponseMapper.toWebViewResponse(any(Response.class))).thenReturn(webResponse);
    }
}