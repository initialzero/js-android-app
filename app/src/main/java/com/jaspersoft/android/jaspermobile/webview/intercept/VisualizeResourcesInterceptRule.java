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

package com.jaspersoft.android.jaspermobile.webview.intercept;

import android.os.Build;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class VisualizeResourcesInterceptRule implements WebResourceInterceptor.Rule {
    private static final String[] RESOURCES = new String[]{"bundles", "scripts", "settings"};

    private static class InstanceHolder {
        private static final VisualizeResourcesInterceptRule INSTANCE = new VisualizeResourcesInterceptRule();
    }

    private VisualizeResourcesInterceptRule() {}

    public static VisualizeResourcesInterceptRule getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public boolean shouldIntercept(WebRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean defaultVale = false;
            String url = request.getUrl().toLowerCase();
            for (String resource : RESOURCES) {
                defaultVale |= url.contains(resource);
            }
            return defaultVale;
        }
        return false;
    }
}
