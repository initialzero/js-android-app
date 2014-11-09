/*
 * The MIT License
 *
 * Copyright (c) 2010 Xtreme Labs and Pivotal Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
