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

import com.jaspersoft.android.jaspermobile.ui.component.presenter.Presenter;
import com.jaspersoft.android.jaspermobile.ui.component.presenter.PresenterControllerDelegate;


public abstract class PresenterControllerActivity<C, P extends Presenter>
        extends ComponentControllerActivity<C> {
    private PresenterControllerDelegate<P> presenterDelegate = new PresenterControllerDelegate<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenterDelegate.onCreate(getPresenter(), savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenterDelegate.onResume();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenterDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenterDelegate.onDestroyView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenterDelegate.onDestroy();
    }

    public abstract P getPresenter();
}
