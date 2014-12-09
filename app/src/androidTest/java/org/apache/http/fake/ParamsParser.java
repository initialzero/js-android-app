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

import android.net.Uri;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamsParser {

    public static Map<String, String> parseParams(HttpRequest request) {
        if (request instanceof HttpGet) {
            return parseParamsForGet(request);
        }
        if (request instanceof HttpEntityEnclosingRequestBase) {
            return parseParamsForRequestWithEntity((HttpEntityEnclosingRequestBase) request);
        }
        return new LinkedHashMap<String, String>();
    }

    private static Map<String, String> parseParamsForRequestWithEntity(HttpEntityEnclosingRequestBase request) {
        try {
            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            HttpEntity entity = request.getEntity();
            if (entity != null) {
                List<NameValuePair> pairs = URLEncodedUtils.parse(entity);

                for (NameValuePair pair : pairs) {
                    map.put(pair.getName(), pair.getValue());
                }
            }
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> parseParamsForGet(HttpRequest request) {
        Uri uri = Uri.parse(request.getRequestLine().getUri());
        Set<String> paramNames = uri.getQueryParameterNames();
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        for (String paramName : paramNames) {
            map.put(paramName, uri.getQueryParameter(paramName));
        }
        return map;
    }
}
