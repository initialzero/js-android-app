package com.jaspersoft.android.jaspermobile.network.cookie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpCookie;
import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class AppCookieStoreTest {

    public static final URI DOMAIN = URI.create("http://localhost");
    public static final HttpCookie HTTP_COOKIE = new HttpCookie("key", "name");
    @Mock
    WebViewCookieStore mWebViewCookieStore;
    @Mock
    java.net.CookieStore mStore;
    @Mock
    org.apache.http.client.CookieStore mLegacyStore;

    private AppCookieStore appCookieStore;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        appCookieStore = new AppCookieStore(mWebViewCookieStore, mStore, mLegacyStore);
    }

    @Test
    public void testAdd() throws Exception {
        appCookieStore.add(DOMAIN, HTTP_COOKIE);
        verify(mStore).add(DOMAIN, HTTP_COOKIE);
        verify(mWebViewCookieStore).add(DOMAIN.toString(), HTTP_COOKIE.toString());
    }

    @Test
    public void testGet() throws Exception {
        appCookieStore.get(DOMAIN);
        verify(mStore).get(DOMAIN);
    }

    @Test
    public void testGetCookies() throws Exception {
        appCookieStore.getCookies();
        verify(mStore).getCookies();
    }

    @Test
    public void testGetURIs() throws Exception {
        appCookieStore.getURIs();
        verify(mStore).getURIs();
    }

    @Test
    public void testRemove() throws Exception {
        appCookieStore.remove(DOMAIN, HTTP_COOKIE);
        verify(mStore).remove(DOMAIN, HTTP_COOKIE);
        verify(mWebViewCookieStore).removeCookie(DOMAIN.toString());
    }

    @Test
    public void testRemoveAll() throws Exception {
        appCookieStore.removeAll();
        verify(mLegacyStore).clear();
        verify(mStore).removeAll();
        verify(mWebViewCookieStore).removeAllCookies();
    }
}