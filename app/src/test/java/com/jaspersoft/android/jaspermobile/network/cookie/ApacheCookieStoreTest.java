/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

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