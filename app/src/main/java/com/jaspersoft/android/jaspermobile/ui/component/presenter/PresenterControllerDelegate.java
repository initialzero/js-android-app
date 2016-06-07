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

package com.jaspersoft.android.jaspermobile.ui.component.presenter;

import android.os.Bundle;

public class PresenterControllerDelegate<P extends Presenter> {
    private boolean isDestroyedBySystem;
    private P presenter;

    public void onCreate(P presenter, Bundle savedInstanceState) {
        this.presenter = presenter;
        PresenterBundle bundle = PresenterBundleUtils.getPresenterBundle(savedInstanceState);
        presenter.onCreate(bundle);
    }

    public void onResume() {
        isDestroyedBySystem = false;
    }

    public void onSaveInstanceState(Bundle outState) {
        isDestroyedBySystem = true;
        PresenterBundle bundle = new PresenterBundle();
        presenter.onSaveInstanceState(bundle);
        PresenterBundleUtils.setPresenterBundle(outState, bundle);
    }

    public void onDestroyView() {
        presenter.pauseView();
    }

    public void onDestroy() {
        if (!isDestroyedBySystem) {
            presenter.unbindView();
        }
    }
}
