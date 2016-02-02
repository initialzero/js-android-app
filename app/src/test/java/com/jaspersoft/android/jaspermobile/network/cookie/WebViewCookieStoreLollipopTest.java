package com.jaspersoft.android.jaspermobile.network.cookie;

import android.webkit.CookieManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21, shadows = {ShadowMultiDex.class})
public class WebViewCookieStoreLollipopTest {
    private static final String DOMAIN = "http://localhost";
    private static final String COOKIE = "key=name";

    @Mock
    CookieManager mCookieManager;
    private WebViewCookieStoreLollipop store;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        store = new WebViewCookieStoreLollipop(mCookieManager);
    }

    @Test
    public void testAdd() throws Exception {
        store.add(DOMAIN, COOKIE);
        verify(mCookieManager).setCookie(DOMAIN, COOKIE);
        verify(mCookieManager).flush();
    }

    @Test
    public void testRemoveCookie() throws Exception {
        store.removeCookie(DOMAIN);
        verify(mCookieManager).setCookie(DOMAIN, null);
        verify(mCookieManager).flush();
    }

    @Test
    public void testRemoveAllCookies() throws Exception {
        store.removeAllCookies();
        verify(mCookieManager).removeAllCookies(null);
        verify(mCookieManager).flush();
    }
}