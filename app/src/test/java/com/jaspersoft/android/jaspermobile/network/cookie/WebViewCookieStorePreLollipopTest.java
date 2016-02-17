package com.jaspersoft.android.jaspermobile.network.cookie;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class WebViewCookieStorePreLollipopTest {
    private static final String DOMAIN = "http://localhost";
    private static final String COOKIE = "key=name";

    @Mock
    CookieManager mCookieManager;
    private CookieSyncManager mCookieSyncManager;
    private WebViewCookieStorePreLollipop store;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mCookieSyncManager = CookieSyncManager.createInstance(RuntimeEnvironment.application);
        store = new WebViewCookieStorePreLollipop(mCookieManager, mCookieSyncManager);
    }

    @Test
    public void testAdd() throws Exception {
        store.add(DOMAIN, COOKIE);
        verify(mCookieManager).setCookie(DOMAIN, COOKIE);
    }

    @Test
    public void testRemoveCookie() throws Exception {
        store.removeCookie(DOMAIN);
        verify(mCookieManager).setCookie(DOMAIN, null);
    }

    @Test
    public void testRemoveAllCookies() throws Exception {
        store.removeAllCookies();
        verify(mCookieManager).removeAllCookie();
    }
}