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

package com.jaspersoft.android.jaspermobile.webview;

import android.app.Activity;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;

import java.lang.ref.WeakReference;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class DefaultSessionListener implements DefaultUrlPolicy.SessionListener {
    private final WeakReference<Activity> weakReference;

    private DefaultSessionListener(Activity activity) {
        this.weakReference = new WeakReference<Activity>(activity);
    }

    public static DefaultUrlPolicy.SessionListener from(Activity activity) {
        return new DefaultSessionListener(activity);
    }

    @Override
    public void onSessionExpired() {
        if (weakReference.get() != null) {
            Toast.makeText(weakReference.get(), R.string.da_session_expired, Toast.LENGTH_LONG).show();
            weakReference.get().finish();
        }
    }
}
