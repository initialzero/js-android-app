/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.utils;

import org.apache.http.message.BasicHeader;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class HttpResponseUtil {
    private static final class HttpResponseUtilHolder {
        private static final HttpResponseUtil INSTANCE = new HttpResponseUtil();
    }

    private HttpResponseUtil() {}

    public static HttpResponseUtil get() {
        return HttpResponseUtilHolder.INSTANCE;
    }

    public TestHttpResponse xmlType(String fileName) {
        BasicHeader contentType = new BasicHeader("Content-Type", "application/xml");
        return new TestHttpResponse(200, TestResources.get().rawData(fileName), contentType);
    }

}
