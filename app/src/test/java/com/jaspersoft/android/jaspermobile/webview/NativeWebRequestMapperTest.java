package com.jaspersoft.android.jaspermobile.webview;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NativeWebRequestMapperTest {

    private static final String ANY_URL = "http://localhost";
    private static final Map<String, String> HEADERS = new HashMap<>();
    static {
        HEADERS.put("Cookie", "jsessionid=123");
    }

    private NativeWebRequestMapper nativeWebRequestMapper;

    @Mock
    WebResourceRequest webResourceRequest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        nativeWebRequestMapper = new NativeWebRequestMapper();
    }

    @Test
    public void should_map_web_resource_request() throws Exception {
        WebRequest webRequest = nativeWebRequestMapper.toGenericRequest(ANY_URL);

        assertThat(webRequest.getUrl(), is(ANY_URL));
        assertThat(webRequest.getMethod(), is(nullValue()));
        assertThat(webRequest.getRequestHeaders(), is(Collections.<String, String>emptyMap()));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void should_map_web_request_url() throws Exception {
        when(webResourceRequest.getUrl()).thenReturn(Uri.parse(ANY_URL));
        when(webResourceRequest.getMethod()).thenReturn("POST");
        when(webResourceRequest.getRequestHeaders()).thenReturn(HEADERS);

        WebRequest webRequest = nativeWebRequestMapper.toGenericRequest(webResourceRequest);

        assertThat(webRequest.getUrl(), is(webResourceRequest.getUrl().toString()));
        assertThat(webRequest.getMethod(), is(webResourceRequest.getMethod()));
        assertThat(webRequest.getRequestHeaders(), is(webResourceRequest.getRequestHeaders()));
    }
}