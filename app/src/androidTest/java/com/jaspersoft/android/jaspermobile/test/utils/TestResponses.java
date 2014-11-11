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

import org.apache.http.fake.TestHttpResponse;

import org.apache.http.message.BasicHeader;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class TestResponses {
    private static final class HttpResponseUtilHolder {
        private static final TestResponses INSTANCE = new TestResponses();
    }

    private TestResponses() {}

    public static TestResponses get() {
        return HttpResponseUtilHolder.INSTANCE;
    }

    public TestHttpResponse xml(String fileName) {
        BasicHeader contentType = new BasicHeader("Content-Type", "application/xml");
        return new TestHttpResponse(200, TestResources.get().rawData(fileName), contentType);
    }

    public TestHttpResponse noContent() {
        return new TestHttpResponse(204, "");
    }

    public TestHttpResponse notAuthorized() {
        return new TestHttpResponse(401, "");
    }

}
