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
import com.google.inject.util.Providers;
import com.jaspersoft.android.jaspermobile.activities.SecurityProviderUpdater;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetInputControlsValuesCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.ValidateInputControlsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.option.DeleteReportOptionCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.option.GetReportOptionValuesCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.option.GetReportOptionsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.option.SaveReportOptionsCase;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.legacy.JsRestClientWrapper;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.sdk.client.JsRestClient;

import javax.inject.Inject;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class  JasperMobileModule extends AbstractModule {
    private final Context mContext;

    @Inject
    JsRestClientWrapper mJsRestClientWrapper;

    public JasperMobileModule(Application application) {
        super();
        mContext = application;
        GraphObject.Factory.from(mContext)
                .getComponent()
                .inject(this);
    }

    @Override
    protected void configure() {
        bind(JsRestClientWrapper.class).toInstance(mJsRestClientWrapper);
        bind(JsRestClient.class).toInstance(mJsRestClientWrapper.getClient());

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

        bind(Context.class).annotatedWith(ApplicationContext.class).toInstance(mContext);
        bind(ReportParamsStorage.class).in(Singleton.class);
        bind(AppConfigurator.class).to(AppConfiguratorImpl.class);
        bind(Analytics.class).toInstance(new JasperAnalytics(mContext));
        bind(SecurityProviderUpdater.class).to(JasperSecurityProviderUpdater.class).in(Singleton.class);

        // TODO clean up mess after roboguice will be removed out
        bind(GetInputControlsValuesCase.class).toProvider(Providers.<GetInputControlsValuesCase>of(null));
        bind(ValidateInputControlsCase.class).toProvider(Providers.<ValidateInputControlsCase>of(null));
        bind(GetReportOptionsCase.class).toProvider(Providers.<GetReportOptionsCase>of(null));
        bind(SaveReportOptionsCase.class).toProvider(Providers.<SaveReportOptionsCase>of(null));
        bind(GetReportOptionValuesCase.class).toProvider(Providers.<GetReportOptionValuesCase>of(null));
        bind(DeleteReportOptionCase.class).toProvider(Providers.<DeleteReportOptionCase>of(null));
        bind(JasperServer.class).toProvider(Providers.<JasperServer>of(null));
    }
}
