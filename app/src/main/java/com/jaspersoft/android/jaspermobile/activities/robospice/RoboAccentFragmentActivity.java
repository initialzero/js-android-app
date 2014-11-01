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

package com.jaspersoft.android.jaspermobile.activities.robospice;

import android.content.res.Resources;
import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;
import com.negusoft.holoaccent.AccentHelper;
import com.negusoft.holoaccent.AccentResources;

import roboguice.activity.RoboFragmentActivity;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class RoboAccentFragmentActivity extends RoboFragmentActivity {

    private final AccentHelper mAccentHelper = new AccentHelper(getOverrideAccentColor(),
            getOverrideAccentColorDark(), getOverrideAccentColorActionBar(), new MyInitListener());

    @Override
    public Resources getResources() {
        return mAccentHelper.getResources(this, super.getResources());
    }

    /**
     * Override this method to set the accent color programmatically.
     * @return The color to override. If the color is equals to 0, the
     * accent color will be taken from the theme.
     */
    public int getOverrideAccentColor() {
        return 0;
    }

    /**
     * Override this method to set the dark variant of the accent color programmatically.
     * @return The color to override. If the color is equals to 0, the dark version will be
     * taken from the theme. If it is specified in the theme either, it will be calculated
     * based on the accent color.
     */
    public int getOverrideAccentColorDark() {
        return 0;
    }

    /**
     * Override this method to set the action bar variant of the accent color programmatically.
     * @return The color to override. If the color is equals to 0, the action bar version will
     * be taken from the theme. If it is specified in the theme either, it will the same as the
     * accent color.
     */
    public int getOverrideAccentColorActionBar() {
        return 0;
    }

    /** Getter for the AccentHelper instance. */
    public AccentHelper getAccentHelper() {
        return mAccentHelper;
    }

    /**
     * Override this function to modify the AccentResources instance. You can add your own logic
     * to the default HoloAccent behaviour.
     */
    public void onInitAccentResources(AccentResources resources) {
        // To be overriden in child classes.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseWrapper.initAndStartSession(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BugSenseWrapper.startSession(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BugSenseWrapper.closeSession(this);
    }

    private class MyInitListener implements AccentHelper.OnInitListener {
        @Override
        public void onInitResources(AccentResources resources) {
            onInitAccentResources(resources);
        }
    }

}
