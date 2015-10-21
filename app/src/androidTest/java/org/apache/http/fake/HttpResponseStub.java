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
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;

import java.util.Locale;

public class HttpResponseStub implements HttpResponse {
    @Override
    public StatusLine getStatusLine() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setStatusLine(StatusLine statusLine) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusLine(ProtocolVersion protocolVersion, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusCode(int i) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReasonPhrase(String s) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpEntity getEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEntity(HttpEntity httpEntity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header[] getHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header getFirstHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header getLastHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header[] getAllHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(Header header) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(Header header) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeaders(Header[] headers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeHeader(Header header) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HeaderIterator headerIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HeaderIterator headerIterator(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpParams getParams() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParams(HttpParams httpParams) {
        throw new UnsupportedOperationException();
    }
}
