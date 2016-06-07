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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ComponentCacheDelegate {
    private static final String NEXT_ID_KEY = "next-presenter-id";

    private NonConfigurationInstance nonConfigurationInstance;

    public void onCreate(Bundle savedInstanceState, Object nonConfigurationInstance) {
        if (nonConfigurationInstance == null) {
            long seed = 0;
            if (savedInstanceState != null) {
                seed = savedInstanceState.getLong(NEXT_ID_KEY);
            }
            this.nonConfigurationInstance = new NonConfigurationInstance(seed);
        } else {
            this.nonConfigurationInstance = (NonConfigurationInstance) nonConfigurationInstance;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(NEXT_ID_KEY, nonConfigurationInstance.nextId.get());
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return nonConfigurationInstance;
    }

    public long generateId() {
        return nonConfigurationInstance.nextId.getAndIncrement();
    }

    @SuppressWarnings("unchecked")
    public final <C> C getComponent(long index) {
        return (C) nonConfigurationInstance.components.get(index);
    }

    public void setComponent(long index, Object component) {
        nonConfigurationInstance.components.put(index, component);
    }

    private static class NonConfigurationInstance {
        private Map<Long, Object> components;
        private AtomicLong nextId;

        public NonConfigurationInstance(long seed) {
            components = new HashMap<>();
            nextId = new AtomicLong(seed);
        }
    }
}
