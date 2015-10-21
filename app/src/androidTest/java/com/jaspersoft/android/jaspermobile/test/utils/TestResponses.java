/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
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
    public static final TestHttpResponse SERVER_INFO = TestResponses.get().xml(TestResources.SERVER_INFO);
    public static final TestHttpResponse EMERALD_MR1_SERVER_INFO = TestResponses.get().xml(TestResources.EMERALD_MR1_SERVER_INFO);
    public static final TestHttpResponse ONLY_DASHBOARD = TestResponses.get().xml(TestResources.ONLY_DASHBOARD);
    public static final TestHttpResponse ONLY_REPORT = TestResponses.get().xml(TestResources.ONLY_REPORT);
    public static final TestHttpResponse ONLY_FOLDER = TestResponses.get().xml(TestResources.ONLY_FOLDER);
    public static final TestHttpResponse BIG_LOOKUP = TestResponses.get().xml(TestResources.BIG_LOOKUP);
    public static final TestHttpResponse SMALL_LOOKUP = TestResponses.get().xml(TestResources.SMALL_LOOKUP);
    public static final TestHttpResponse ROOT_FOLDER = TestResponses.get().xml(TestResources.ROOT_FOLDER);
    public static final TestHttpResponse ROOT_REPOSITORIES = TestResponses.get().xml(TestResources.ROOT_REPOSITORIES);
    public static final TestHttpResponse REPORT_EXECUTION = TestResponses.get().xml(TestResources.REPORT_EXECUTION);
    public static final TestHttpResponse INPUT_CONTROLS = TestResponses.get().xml(TestResources.INPUT_CONTROLS);

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
