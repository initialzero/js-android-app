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

package com.jaspersoft.android.jaspermobile.ui.component.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.ui.component.ComponentControllerDelegate;
import com.jaspersoft.android.jaspermobile.ui.component.ComponentFactory;

public abstract class ComponentControllerActivity<C> extends ComponentCacheActivity {
    private ComponentControllerDelegate<C> componentDelegate = new ComponentControllerDelegate<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        componentDelegate.onCreate(this, savedInstanceState, componentFactory);
    }

    @Override
    protected void onResume() {
        super.onResume();
        componentDelegate.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        componentDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        componentDelegate.onDestroy();
    }

    public C getComponent() {
        return componentDelegate.getComponent();
    }

    protected abstract C onCreateNonConfigurationComponent();

    private ComponentFactory<C> componentFactory = new ComponentFactory<C>() {
        @NonNull
        @Override
        public C createComponent() {
            return onCreateNonConfigurationComponent();
        }
    };
}