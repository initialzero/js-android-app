package com.jaspersoft.android.jaspermobile.webview.intercept.okhttp;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;
import com.jaspersoft.android.jaspermobile.webview.WebResponse;
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

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class OkResponseMapperTest {
    private static final String ANY_URL = "http://localhost";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_ENCODING = "Content-Encoding";

    @Mock
    WebRequest request;

    private WebResponse mappedResponse;
    private Response okResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_filter_header() throws Exception {
        givenResponse();

        whenMapsResponse();

        assertThat(mappedResponse.getMimeType(), is("application/json"));
        assertThat(mappedResponse.getEncoding(), is("utf-8"));
        assertThat(mappedResponse.getStatusCode(), is(200));
        assertThat(mappedResponse.getReasonPhrase(), is("OK"));
        assertThat(mappedResponse.getData(), is(nullValue()));
        assertThat(mappedResponse.getResponseHeaders(), is(notNullValue()));
    }

    private void givenResponse() {
        Request okRequest = new Request.Builder()
                .url(ANY_URL)
                .build();
        okResponse = new Response.Builder()
                .protocol(Protocol.HTTP_1_1)
                .request(okRequest)
                .message("OK")
                .addHeader(CONTENT_TYPE, "application/json")
                .addHeader(CONTENT_ENCODING, "utf-8")
                .code(200)
                .build();
    }

    private void whenMapsResponse() {
        OkResponseMapper okResponseMapper = new OkResponseMapper();
        mappedResponse = okResponseMapper.toWebViewResponse(okResponse);
    }
}