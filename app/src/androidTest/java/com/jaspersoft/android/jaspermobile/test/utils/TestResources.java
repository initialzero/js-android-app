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

/**
 * @author Tom Koptel
 * @since 1.9
 */
@Deprecated
public final class TestResources {
    public static final String SERVER_INFO = "server_info";
    public static final String EMERALD_MR1_SERVER_INFO = "emerald_mr1_server_info";
    public static final String ONLY_DASHBOARD = "only_dashboard";
    public static final String ONLY_REPORT = "only_report";
    public static final String ONLY_FOLDER = "level_repositories";
    public static final String ALL_RESOURCES = "all_resources";
    public static final String BIG_LOOKUP = "library_0_40";
    public static final String SMALL_LOOKUP = "library_reports_small";
    public static final String ROOT_FOLDER = "root_folder";
    public static final String ROOT_REPOSITORIES = "root_repositories";
    public static final String REPORT_EXECUTION = "report_execution";
    public static final String INPUT_CONTROLS = "input_contols_list";

    public static TestResource get() {
        return TestResource.get(TestResource.DataFormat.XML);
    }
}
