/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile;

import android.app.Application;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.1
 */
public interface Analytics {
    void init(Application appContext);
    void setScreenName(String screenName);
    void sendScreenView(String screenName, List<Dimension> dimension);
    void sendEvent(String eventCategory, String eventAction, String eventLabel);
    void sendUserChangedEvent();
    void setServerInfo(String serverVersion, String serverEdition);

    enum ScreenName {
        LIBRARY("Library screen"),
        REPOSITORY("Repository screen"),
        RECENTLY_VIEWED("Recently viewed screen"),
        FAVORITES("Favorites screen"),
        SAVED_ITEMS("Saved items screen");

        String mName;

        ScreenName(String name) {
            mName = name;
        }

        public String getValue() {
            return mName;
        }
    }

    enum EventCategory {
        MENU("Menu"),
        ACCOUNT("Account"),
        CATALOG("Catalog"),
        RESOURCE("Resource");

        String mName;

        EventCategory(String name) {
            mName = name;
        }

        public String getValue() {
            return mName;
        }
    }

    enum EventAction {
        OPENED("Opened"),
        CLICKED("Clicked"),
        VIEWED("Viewed"),
        REFRESHED("Refreshed"),
        LOADED_NEXT("Loaded next"),
        SORTED("Sorted"),
        FILTERED("Filtered"),
        CHANGED_VIEW_TYPE("Changed view type"),
        CHANGED("Changed"),
        PRINTED("Printed");

        String mName;

        EventAction(String name) {
            mName = name;
        }

        public String getValue() {
            return mName;
        }
    }

    enum EventLabel {
        LIBRARY("Library"),
        REPOSITORY("Repository"),
        RECENTLY_VIEWED("Recently viewed"),
        FAVORITES("Favorites"),
        SAVED_ITEMS("Saved items"),
        ADD_ACCOUNT("Add account"),
        SWITCH_ACCOUNT("Switch account"),
        SETTINGS("Settings"),
        FEEDBACK("Feedback"),
        ABOUT("About"),
        MANAGE_ACCOUNT("Manage account"),
        REPORT("Report"),
        DASHBOARD("Dashboard");

        String mName;

        EventLabel(String name) {
            mName = name;
        }

        public String getValue() {
            return mName;
        }
    }

    class Dimension {
        public static final int FILTER_TYPE_KEY = 3;
        public static final int RESOURCE_VIEW_KEY = 4;

        private int mKey;
        private String mValue;

        public Dimension(int key, String value) {
            this.mKey = key;
            this.mValue = value;
        }

        public int getKey() {
            return mKey;
        }

        public String getValue() {
            return mValue;
        }
    }
}
