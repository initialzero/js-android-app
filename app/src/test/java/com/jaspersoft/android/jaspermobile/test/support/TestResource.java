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

package com.jaspersoft.android.jaspermobile.test.support;

import android.text.TextUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class TestResource {
    private final String fileName;

    private TestResource(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("Resource name should not be null");
        }
        URL path = getClass().getClassLoader().getResource(fileName);
        if (path == null) {
            throw new IllegalStateException(this + " is missing");
        }
        this.fileName = fileName;
    }

    public static TestResource get(String fileName) {
        return new TestResource(fileName);
    }

    public String asString() {
        InputStream inputStream = asStream();

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

    public InputStream asStream() {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    @Override
    public String toString() {
        return "TestResource{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
