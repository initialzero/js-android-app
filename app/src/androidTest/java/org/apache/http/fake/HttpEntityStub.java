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

package org.apache.http.fake;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpEntityStub implements HttpEntity {
    @Override public boolean isRepeatable() {
        return true;
    }

    @Override public boolean isChunked() {
        throw new UnsupportedOperationException();
    }

    @Override public long getContentLength() {
        throw new UnsupportedOperationException();
    }

    @Override public Header getContentType() {
        throw new UnsupportedOperationException();
    }

    @Override public Header getContentEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override public InputStream getContent() throws IOException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override public void writeTo(OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public boolean isStreaming() {
        throw new UnsupportedOperationException();
    }

    @Override public void consumeContent() throws IOException {
        throw new UnsupportedOperationException();
    }

    public static interface ResponseRule {
        boolean matches(HttpRequest request);

        HttpResponse getResponse() throws HttpException, IOException;
    }
}
