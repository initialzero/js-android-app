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

package com.jaspersoft.android.jaspermobile.ui.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.BaseActivityComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.ComponentProviderDelegate;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toggleScreenCapturing();
    }

    @NonNull
    protected AppComponent getAppComponent() {
        return GraphObject.Factory.from(this).getComponent();
    }

    @NonNull
    protected ProfileComponent getProfileComponent() {
        return ComponentProviderDelegate.INSTANCE.getProfileComponent(this);
    }

    @NonNull
    protected BaseActivityComponent getBaseActivityComponent() {
        return ComponentProviderDelegate.INSTANCE.getBaseActivityComponent(this);
    }

    private void toggleScreenCapturing(){
        boolean isScreenCaptureEnable = DefaultPrefHelper_.getInstance_(this).isScreenCapturingEnabled();

        if (!isScreenCaptureEnable) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }
    }
}
