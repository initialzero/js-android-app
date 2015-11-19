/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.androidannotations.api.sharedpreferences.EditorHelper;
import org.androidannotations.api.sharedpreferences.SharedPreferencesHelper;
import org.androidannotations.api.sharedpreferences.StringPrefEditorField;
import org.androidannotations.api.sharedpreferences.StringPrefField;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ControllerPref extends SharedPreferencesHelper {

    public ControllerPref(Context context, String tag) {
        super(context.getSharedPreferences(tag, 0));
    }

    public ControllerPref.ControllerPrefEditor_ edit() {
        return new ControllerPref.ControllerPrefEditor_(getSharedPreferences());
    }

    public StringPrefField viewType() {
        return stringField("viewType", "LIST");
    }

    public final static class ControllerPrefEditor_
            extends EditorHelper<ControllerPrefEditor_> {

        ControllerPrefEditor_(SharedPreferences sharedPreferences) {
            super(sharedPreferences);
        }

        public StringPrefEditorField<ControllerPrefEditor_> viewType() {
            return stringField("viewType");
        }

    }
}
