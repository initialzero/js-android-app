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

package org.apache.http.hacked;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.fake.FakeHttpLayerManager;
import org.apache.http.fake.HttpRequestInfo;
import org.apache.http.fake.Util;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class HackedDefaultRequestDirector extends DefaultRequestDirector {
    public HackedDefaultRequestDirector(HttpRequestExecutor requestExec, ClientConnectionManager conman,
                                        ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat,
                                        HttpRoutePlanner rouplan, HttpProcessor httpProcessor,
                                        HttpRequestRetryHandler retryHandler, RedirectHandler redirectHandler,
                                        AuthenticationHandler targetAuthHandler, AuthenticationHandler proxyAuthHandler,
                                        UserTokenHandler userTokenHandler, HttpParams params) {
        super(requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler, redirectHandler, targetAuthHandler, proxyAuthHandler, userTokenHandler, params);
    }

    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        if (FakeHttpLayerManager.getFakeHttpLayer().isInterceptingHttpRequests()) {
            return FakeHttpLayerManager.getFakeHttpLayer().emulateRequest(httpHost, httpRequest, httpContext, this);
        } else {
            FakeHttpLayerManager.getFakeHttpLayer().addRequestInfo(new HttpRequestInfo(httpRequest, httpHost, httpContext, this));
            HttpResponse response = super.execute(httpHost, httpRequest, httpContext);

            if (FakeHttpLayerManager.getFakeHttpLayer().isInterceptingResponseContent()) {
                interceptResponseContent(response);
            }

            FakeHttpLayerManager.getFakeHttpLayer().addHttpResponse(response);
            return response;
        }
    }

    private void interceptResponseContent(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        if (entity instanceof HttpEntityWrapper) {
            HttpEntityWrapper entityWrapper = (HttpEntityWrapper) entity;
            try {
                Field wrappedEntity = HttpEntityWrapper.class.getDeclaredField("wrappedEntity");
                wrappedEntity.setAccessible(true);
                entity = (HttpEntity) wrappedEntity.get(entityWrapper);
            } catch (Exception e) {
                // fail to record
            }
        }
        if (entity instanceof BasicHttpEntity) {
            BasicHttpEntity basicEntity = (BasicHttpEntity) entity;
            try {
                Field contentField = BasicHttpEntity.class.getDeclaredField("content");
                contentField.setAccessible(true);
                InputStream content = (InputStream) contentField.get(basicEntity);

                byte[] buffer = Util.readBytes(content);

                FakeHttpLayerManager.getFakeHttpLayer().addHttpResponseContent(buffer);
                contentField.set(basicEntity, new ByteArrayInputStream(buffer));
            } catch (Exception e) {
                // fail to record
            }
        }
    }
}
