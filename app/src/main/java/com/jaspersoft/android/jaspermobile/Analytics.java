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

/**
 * @author Andrew Tivodar
 * @since 2.1
 */
public interface Analytics {
    void init(Application appContext);
    void sendScreenView(String screenName);
    void sendEvent(String eventCategory, String eventAction, String eventLabel);
    void sendEvent(String eventCategory, String eventAction, String eventLabel, Dimension dimension);
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
        ACCOUNT("Account"),
        LIBRARY("Library"),
        REPOSITORY("Repository"),
        RECENTLY_VIEWED("Recently viewed"),
        FAVORITES("Favorites"),
        SAVED_ITEMS("Saved items"),
        SETTINGS("Settings"),
        FEEDBACK("Feedback"),
        ABOUT("About"),
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
        OPEN("Open"),
        SORT("Sort"),
        FILTER("Filter"),
        VIEW_TYPE("Change view type"),
        ADD("Add"),
        MANAGE("Manage"),
        CHANGED("Changed"),
        PRINT("Print");

        String mName;

        EventAction(String name) {
            mName = name;
        }

        public String getValue() {
            return mName;
        }
    }

    enum EventLabel {
        CLICK("Click"),
        PASSIVE("Passive");

        String mName;

        EventLabel(String name) {
            mName = name;
        }

        public String getValue() {
            return mName;
        }
    }

    class Dimension {
        private int mIndex;
        private String mValue;

        public Dimension(int index, String value) {
            this.mIndex = index;
            this.mValue = value;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getValue() {
            return mValue;
        }
    }
}
