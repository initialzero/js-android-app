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

import org.apache.commons.io.IOUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public final class TestResources {
    public static final String SERVER_INFO = "server_info";
    public static final String EMERALD_MR1_SERVER_INFO = "emerald_mr1_server_info";
    public static final String ONLY_DASHBOARD = "only_dashboard";
    public static final String ONLY_REPORT = "only_report";
    public static final String ONLY_FOLDER = "level_repositories";
    public static final String ALL_RESOURCES = "library_reports_small";
    public static final String BIG_LOOKUP = "library_0_40";
    public static final String SMALL_LOOKUP = "library_reports_small";
    public static final String ROOT_FOLDER = "root_folder";
    public static final String ROOT_REPOSITORIES = "root_repositories";
    public static final String REPORT_EXECUTION = "report_execution";
    public static final String INPUT_CONTROLS = "input_contols_list";

    private TestResources() {
    }

    private static class Holder {
        private static final TestResources INSTANCE = new TestResources();
    }

    public <T> T fromXML(Class<T> clazz, String fileName) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName + ".xml");

        Serializer serializer = new Persister();
        try {
            return serializer.read(clazz, stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String rawData(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName + ".xml");
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return writer.toString();
    }

    public static TestResources get() {
        return Holder.INSTANCE;
    }
}
