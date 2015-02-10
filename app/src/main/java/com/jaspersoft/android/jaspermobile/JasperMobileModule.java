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

package com.jaspersoft.android.jaspermobile;

import android.app.Application;
import android.content.Context;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.jaspersoft.android.jaspermobile.legacy.TokenHttpRequestInterceptor;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.util.KeepAliveHttpRequestInterceptor;
import com.jaspersoft.android.sdk.util.LocalesHttpRequestInterceptor;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class JasperMobileModule extends AbstractModule {
    private final Context mContext;

    public JasperMobileModule(Application application) {
        super();
        mContext = application;
    }

    @Override
    protected void configure() {
        JsRestClient jsRestClient = new JsRestClient();
        List<ClientHttpRequestInterceptor> interceptors = Lists.newArrayList();
        interceptors.add(new LocalesHttpRequestInterceptor());
        interceptors.add(new TokenHttpRequestInterceptor(mContext));
        interceptors.add(new KeepAliveHttpRequestInterceptor());
        jsRestClient.setRequestInterceptors(interceptors);
        bind(JsRestClient.class).toInstance(jsRestClient);

        int animationSpeed = mContext.getResources().getInteger(
                android.R.integer.config_longAnimTime);
        animationSpeed *= 1.5;
        bindConstant().annotatedWith(Names.named("animationSpeed"))
                .to(animationSpeed);
        bindConstant().annotatedWith(Names.named("LIMIT")).to(100);
        bindConstant().annotatedWith(Names.named("MAX_PAGE_ALLOWED")).to(10);
        bindConstant().annotatedWith(Names.named("THRESHOLD")).to(5);
    }

}
