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

package com.jaspersoft.android.jaspermobile.test.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.jaspersoft.android.jaspermobile.info.ServerInfoManager;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class CommonTestModule extends AbstractModule {
    @Override
    protected final void configure() {
        bind(ServerInfoManager.class).in(Singleton.class);
        bindConstant().annotatedWith(Names.named("animationSpeed")).to(0);
        bindConstant().annotatedWith(Names.named("LIMIT")).to(40);
        bindConstant().annotatedWith(Names.named("THRESHOLD")).to(5);
        semanticConfigure();
    }

    protected abstract void semanticConfigure();
}
