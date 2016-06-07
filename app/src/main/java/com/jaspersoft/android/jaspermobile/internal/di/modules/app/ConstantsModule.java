/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.internal.di.modules.app;

import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class ConstantsModule {
    @Provides
    @Singleton
    @Named("accountType")
    String provideAccountType() {
        return JasperSettings.JASPER_ACCOUNT_TYPE;
    }

    @Provides
    @Singleton
    @Named("reserved_account_name")
    String provideReservedAccountName() {
        return JasperSettings.RESERVED_ACCOUNT_NAME;
    }

    @Provides
    @Named("LIMIT")
    @Singleton
    Integer provideLimit() {
        return 100;
    }

    @Provides
    @Named("THRESHOLD")
    @Singleton
    Integer provideThreshold() {
        return 5;
    }
}
