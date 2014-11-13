/*
 * Copyright (c) 2014. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.activities.repository.support;

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
