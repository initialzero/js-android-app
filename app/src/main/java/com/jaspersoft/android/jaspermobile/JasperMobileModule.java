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

package com.jaspersoft.android.jaspermobile;

import android.app.Application;
import android.content.Context;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.jaspersoft.android.jaspermobile.activities.SecurityProviderUpdater;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializer;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializerImpl;
import com.jaspersoft.android.jaspermobile.legacy.TokenHttpRequestInterceptor;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.util.KeepAliveHttpRequestInterceptor;
import com.jaspersoft.android.sdk.util.LocalesHttpRequestInterceptor;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.util.ArrayList;
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
        JsRestClient jsRestClient = JsRestClient.builder().setDataType(JsRestClient.DataType.JSON).build();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
        interceptors.add(new LocalesHttpRequestInterceptor());
        interceptors.add(new TokenHttpRequestInterceptor(mContext));
        interceptors.add(new KeepAliveHttpRequestInterceptor());
        jsRestClient.setRequestInterceptors(interceptors);
        jsRestClient.setConnectTimeout(DefaultPrefHelper_.getInstance_(mContext).getConnectTimeoutValue());
        jsRestClient.setReadTimeout(DefaultPrefHelper_.getInstance_(mContext).getReadTimeoutValue());
        bind(JsRestClient.class).toInstance(jsRestClient);

        int animationSpeed = mContext.getResources().getInteger(
                android.R.integer.config_longAnimTime);
        animationSpeed *= 1.5;
        bindConstant().annotatedWith(Names.named("animationSpeed"))
                .to(animationSpeed);
        bindConstant().annotatedWith(Names.named("LIMIT")).to(100);
        bindConstant().annotatedWith(Names.named("MAX_PAGE_ALLOWED")).to(10);
        bindConstant().annotatedWith(Names.named("THRESHOLD")).to(5);

        String endpoint = AccountServerData.Demo.SERVER_URL;
        bindConstant().annotatedWith(Names.named("DEMO_ENDPOINT")).to(endpoint);

        bind(ReportParamsStorage.class).in(Singleton.class);
        bind(ReportParamsSerializer.class).to(ReportParamsSerializerImpl.class);
        bind(AppConfigurator.class).to(AppConfiguratorImpl.class);
        bind(Analytics.class).toInstance(new JasperAnalytics(mContext));
        bind(SecurityProviderUpdater.class).to(JasperSecurityProviderUpdater.class).in(Singleton.class);
    }

}
