package com.jaspersoft.android.jaspermobile.webview.intercept.okhttp;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;
import com.squareup.okhttp.Request;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(JUnitParamsRunner.class)
public class OkRequestMapperTest {
    private static final String ANY_URL = "http://localhost";

    @Mock
    WebRequest request;

    private Request mappedRequest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Parameters({"Pragma", "Cache-Control", "User-Agent"})
    public void should_filter_header(String headerName) throws Exception {
        givenRequestWithHeader(headerName);

        whenMapsRequest();

        thenRequestShouldNotContainsHeader(headerName);
    }

    private void givenRequestWithHeader(String header) {
        Map<String, String> headers = new HashMap<>();
        headers.put(header, null);
        when(request.getRequestHeaders()).thenReturn(headers);
        when(request.getUrl()).thenReturn(ANY_URL);
    }

    private void whenMapsRequest() {
        OkRequestMapper okRequestMapper = new OkRequestMapper();
        mappedRequest = okRequestMapper.toOkHttpRequest(request);
    }

    private void thenRequestShouldNotContainsHeader(String headerName) {
        assertThat(mappedRequest.headers(headerName), is(empty()));
    }
}