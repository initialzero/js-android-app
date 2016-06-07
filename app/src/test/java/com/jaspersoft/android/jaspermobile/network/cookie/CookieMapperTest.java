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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpCookie;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class CookieMapperTest {

    private CookieMapper cookieMapper;
    private HttpCookie fakeCookie;

    @Before
    public void setUp() throws Exception {
        cookieMapper = new CookieMapper();
        fakeCookie = new HttpCookie("key", "domain");
        fakeCookie.setPath("/path");
        fakeCookie.setDomain("localhost");
        fakeCookie.setVersion(1);
    }

    @Test
    public void testToApacheCookie() throws Exception {
        Cookie cookie = cookieMapper.toApacheCookie(fakeCookie);
        assertThat("Failed to map cookie path", cookie.getPath(), is(fakeCookie.getPath()));
        assertThat("Failed to map cookie domain", cookie.getDomain(), is(fakeCookie.getDomain()));
        assertThat("Failed to map cookie version", cookie.getVersion(), is(fakeCookie.getVersion()));
        assertThat("Failed to map cookie value", cookie.getValue(), is(fakeCookie.getValue()));
        assertThat("Failed to map cookie name", cookie.getName(), is(fakeCookie.getName()));
        assertThat("Failed to map cookie expiry date", cookie.getExpiryDate(), is(notNullValue()));
    }
}