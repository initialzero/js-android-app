package com.jaspersoft.android.jaspermobile.network.cookie;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class PersistentCookieStoreTest {
    public static final URI DOMAIN = URI.create("http://localhost");
    public static final URI DOMAIN_WITH_PATH = URI.create("http://localhost/path");
    private static final String SP_COOKIE_STORE = "cookieStore";
    private static final String SP_KEY_DELIMITER = "|";

    private static HttpCookie FAKE_COOKIE;
    static {
        FAKE_COOKIE = new HttpCookie("key", "domain");
        FAKE_COOKIE.setPath("/path");
        FAKE_COOKIE.setDomain("localhost");
        FAKE_COOKIE.setVersion(1);
        FAKE_COOKIE.setMaxAge(TimeUnit.DAYS.toMillis(1));
    }
    private static final String ASSERT_KEY = DOMAIN.toString() + FAKE_COOKIE.getPath() + SP_KEY_DELIMITER + FAKE_COOKIE.getName();

    private PersistentCookieStore persistentCookieStore;
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() throws Exception {
        Application context = RuntimeEnvironment.application;
        persistentCookieStore = new PersistentCookieStore(context);
        sharedPreferences = context.getSharedPreferences(SP_COOKIE_STORE, Context.MODE_PRIVATE);
    }

    @Test
    public void testAdd() throws Exception {
        persistentCookieStore.add(DOMAIN, FAKE_COOKIE);
        boolean containsCookie = sharedPreferences.contains(ASSERT_KEY);
        assertThat("Failed to save add cookie", containsCookie, is(true));
    }

    @Test
    public void testGet() throws Exception {
        persistentCookieStore.add(DOMAIN, FAKE_COOKIE);
        List<HttpCookie> cookies = persistentCookieStore.get(DOMAIN_WITH_PATH);
        assertThat("Failed to get cookie", cookies, hasItem(FAKE_COOKIE));
    }

    @Test
    public void testGetCookies() throws Exception {
        persistentCookieStore.add(DOMAIN, FAKE_COOKIE);
        List<HttpCookie> cookies = persistentCookieStore.getCookies();
        assertThat(cookies, hasItem(FAKE_COOKIE));
    }

    @Test
    public void testGetURIs() throws Exception {
        persistentCookieStore.add(DOMAIN_WITH_PATH, FAKE_COOKIE);
        List<URI> uris = persistentCookieStore.getURIs();
        assertThat("Failed to get uris", uris, hasItem(DOMAIN_WITH_PATH));
    }

    @Test
    public void testRemove() throws Exception {
        persistentCookieStore.add(DOMAIN, FAKE_COOKIE);
        persistentCookieStore.remove(DOMAIN_WITH_PATH, FAKE_COOKIE);

        boolean containsCookie = sharedPreferences.contains(ASSERT_KEY);
        assertThat(containsCookie, is(false));
    }

    @Test
    public void testRemoveAll() throws Exception {
        persistentCookieStore.add(DOMAIN, FAKE_COOKIE);
        persistentCookieStore.removeAll();

        boolean containsCookie = sharedPreferences.contains(ASSERT_KEY);
        assertThat(containsCookie, is(false));
    }
}