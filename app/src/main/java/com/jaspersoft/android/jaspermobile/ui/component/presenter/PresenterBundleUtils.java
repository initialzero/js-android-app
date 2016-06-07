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
import android.util.Log;

import java.util.HashMap;

class PresenterBundleUtils {
    private static final String TAG = PresenterBundleUtils.class.getSimpleName();
    private static final String MAP_KEY = PresenterBundle.class.getName();

    private PresenterBundleUtils() {
        // No instances
    }

    @SuppressWarnings("unchecked") // Handled internally
    public static PresenterBundle getPresenterBundle(Bundle savedInstanceState) {
        HashMap<String, Object> map = null;
        if (savedInstanceState != null) {
            try {
                map = (HashMap<String, Object>) savedInstanceState
                        .getSerializable(MAP_KEY);
            } catch (ClassCastException e) {
                Log.e(TAG, "", e);
            }
        }
        PresenterBundle result = null;
        if (map != null) {
            result = new PresenterBundle();
            result.setMap(map);
        }
        return result;
    }

    public static void setPresenterBundle(Bundle outState, PresenterBundle presenterBundle) {
        outState.putSerializable(MAP_KEY, presenterBundle.getMap());
    }
}
