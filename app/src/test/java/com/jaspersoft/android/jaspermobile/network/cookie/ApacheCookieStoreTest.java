package com.jaspersoft.android.jaspermobile.network.cookie;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class ApacheCookieStoreTest {

    private static final List<HttpCookie> COOKIES =
            Collections.singletonList(new HttpCookie("key", "value"));
    private static final Cookie LEGACY_COOKIE = new BasicClientCookie("key", "value");

    @Mock
    CookieStore mCookieStore;
    @Mock
    CookieMapper mCookieMapper;

    private ApacheCookieStore apacheCookieStore;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        apacheCookieStore = new ApacheCookieStore(mCookieStore, mCookieMapper);
    }

    @Test
    public void testGetCookies() throws Exception {
        when(mCookieMapper.toApacheCookie(any(HttpCookie.class))).thenReturn(LEGACY_COOKIE);
        when(mCookieStore.getCookies()).thenReturn(COOKIES);

        List<Cookie> cookies = apacheCookieStore.getCookies();
        assertThat(cookies, hasItem(LEGACY_COOKIE));

        verify(mCookieStore).getCookies();
        verify(mCookieMapper).toApacheCookie(COOKIES.get(0));
    }
}