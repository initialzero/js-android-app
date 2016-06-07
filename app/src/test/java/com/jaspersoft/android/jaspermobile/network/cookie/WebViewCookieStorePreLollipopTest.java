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