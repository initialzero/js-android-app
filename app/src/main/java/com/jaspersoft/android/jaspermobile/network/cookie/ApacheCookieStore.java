package com.jaspersoft.android.jaspermobile.network.cookie;

import org.apache.http.cookie.Cookie;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class ApacheCookieStore implements org.apache.http.client.CookieStore {
    private final CookieStore mCookieStore;
    private final CookieMapper mCookieMapper;

    public ApacheCookieStore(CookieStore cookieStore, CookieMapper cookieMapper) {
        mCookieStore = cookieStore;
        mCookieMapper = cookieMapper;
    }

    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public List<Cookie> getCookies() {
        List<HttpCookie> cookies = mCookieStore.getCookies();
        List<Cookie> result = new ArrayList<>(cookies.size());
        for (HttpCookie cookie : cookies) {
            result.add(mCookieMapper.toApacheCookie(cookie));
        }
        return result;
    }

    @Override
    public boolean clearExpired(Date date) {
        return false;
    }

    @Override
    public void clear() {
    }
}
