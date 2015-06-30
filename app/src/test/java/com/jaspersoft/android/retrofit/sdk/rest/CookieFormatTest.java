/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.retrofit.sdk.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import retrofit.client.Header;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CookieFormatTest {
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();

    private static final Header header1 = new Header("Set-Cookie", "cookie1");
    private static final Header header2 = new Header("Set-Cookie", "cookie2");
    private static final Header header3 = new Header("Strange-Header", "header");

    private CookieFormat.Formatter cookieFormatter;

    @Before
    public void setup() {
        cookieFormatter = CookieFormat.Formatter.INSTANCE;
    }

    @Test
    public void shouldFilterOnlySet_CookieHeader() {
        List<Header> headers = new ArrayList<Header>() {{
            add(header1);
            add(header2);
            add(header3);
        }};

        List<Header> result = cookieFormatter.filterCookieHeaders(headers);

        assertThat(result, hasItem(header1));
        assertThat(result, hasItem(header2));
    }

    @Test
    public void shouldJoinCookieHeaders() {
        List<Header> headers = new ArrayList<Header>() {{
            add(header1);
            add(header2);
        }};

        StringBuilder result = cookieFormatter.joinCookieHeaders(headers);

        assertThat(result.toString(), is("cookie1;cookie2"));
    }

    @Test
    public void shouldAppendTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Helsinki"));

        StringBuilder result = new StringBuilder();
        cookieFormatter.appendTimeZone(result);

        assertThat(result.toString(), is(";userTimezone=Europe/Helsinki"));

        // Just in case any of the tests mess with the system-wide
        // default time zone, make sure we've set it back to what
        // it should be.
        TimeZone.setDefault(DEFAULT_TIMEZONE);
    }

    @Test
    public void shouldFormatHeaders() {
        List<Header> headers = new ArrayList<Header>() {{
            add(header1);
            add(header2);
        }};

        String format = CookieFormat.format(headers);

        assertThat(format, is(notNullValue()));
        assertThat(format.trim().length(), is(not(0)));
    }
}
