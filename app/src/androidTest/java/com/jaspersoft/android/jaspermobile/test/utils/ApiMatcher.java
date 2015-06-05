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

package com.jaspersoft.android.jaspermobile.test.utils;

import org.apache.http.fake.RequestMatcher;
import org.apache.http.hacked.GetUriRegexMatcher;
import org.apache.http.hacked.PostUriRegexMatcher;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ApiMatcher {
    public static final RequestMatcher SERVER_INFO = new GetUriRegexMatcher(".*/(serverInfo)$");
    public static final RequestMatcher RESOURCES = new GetUriRegexMatcher(".*/(resources).*");
    public static final RequestMatcher GET_ROOT_FOLDER = new GetUriRegexMatcher(".*/(resources)$");
    public static final RequestMatcher ROOT_FOLDER_CONTENT = new GetUriRegexMatcher(".*(folderUri=/).*");
    public static final RequestMatcher REPORTS_QUERY = new GetUriRegexMatcher(".*(q=Reports).*");
    public static final RequestMatcher INPUT_CONTROLS = new PostUriRegexMatcher(".*/(inputControls)$");
    public static final RequestMatcher REPORT_EXECUTIONS = new PostUriRegexMatcher(".*/(reportExecutions)$");
    public static final RequestMatcher OUTPUT_RESOURCE = new GetUriRegexMatcher(".*/(outputResource)$");
    public static final RequestMatcher REPORTS = new PostUriRegexMatcher(".*/(reports).*");

    private ApiMatcher() {}
}
