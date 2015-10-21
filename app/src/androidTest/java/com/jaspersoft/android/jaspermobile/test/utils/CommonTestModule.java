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

package com.jaspersoft.android.jaspermobile.test.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializer;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class CommonTestModule extends AbstractModule {
    @Override
    protected void configure() {
        commonConfigurations();
        commonStrictConfigurations();
    }

    protected void commonStrictConfigurations() {
        bindConstant().annotatedWith(Names.named("DEMO_ENDPOINT")).to(AccountServerData.Demo.SERVER_URL);
    }

    protected void commonConfigurations() {
        bindConstant().annotatedWith(Names.named("animationSpeed")).to(0);
        bindConstant().annotatedWith(Names.named("LIMIT")).to(40);
        bindConstant().annotatedWith(Names.named("THRESHOLD")).to(5);
        bindConstant().annotatedWith(Names.named("MAX_PAGE_ALLOWED")).to(1);
        bind(ReportParamsStorage.class).in(Singleton.class);
        bind(ReportParamsSerializer.class).to(ReportParamsSerializer.class);
    }
}
