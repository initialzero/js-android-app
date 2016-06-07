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

package com.jaspersoft.android.jaspermobile.ui.component;

import android.os.Bundle;

public class ComponentControllerDelegate<C> {
    private static final String PRESENTER_INDEX_KEY = "presenter-index";

    private C component;
    private ComponentCache cache;
    private long componentId;
    private boolean isDestroyedBySystem;

    public void onCreate(ComponentCache cache, Bundle savedInstanceState,
                         ComponentFactory<C> componentFactory) {
        this.cache = cache;
        if (savedInstanceState == null) {
            componentId = cache.generateId();
        } else {
            componentId = savedInstanceState.getLong(PRESENTER_INDEX_KEY);
        }
        component = cache.getComponent(componentId);
        if (component == null) {
            component = componentFactory.createComponent();
            cache.setComponent(componentId, component);
        }
    }

    public void onResume() {
        isDestroyedBySystem = false;
    }

    public void onSaveInstanceState(Bundle outState) {
        isDestroyedBySystem = true;
        outState.putLong(PRESENTER_INDEX_KEY, componentId);
    }

    public void onDestroy() {
        if (!isDestroyedBySystem) {
            // User is exiting this view, remove component from the cache
            cache.setComponent(componentId, null);
        }
    }

    public C getComponent() {
        return component;
    }
}
