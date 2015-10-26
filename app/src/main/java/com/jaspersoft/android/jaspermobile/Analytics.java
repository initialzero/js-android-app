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
    void sendScreenView(String categoryName);
    void sendEvent(String eventCategory, String eventAction, String eventLabel);
    void sendUserChangedEvent();
    void setServerInfo(String serverVersion, String serverEdition);

    enum EventCategory{
        PRINT("Print"),
        MENU("Menu");

        String mName;

        EventCategory(String name) {
            mName = name;
        }

        public String getValue(){
            return mName;
        }
    }

    enum EventAction{
        CLICK("Click");

        String mName;

        EventAction(String name) {
            mName = name;
        }

        public String getValue(){
            return mName;
        }
    }

    enum EventLabel{
        LIBRARY("Library"),
        REPOSITORY("Repository"),
        RECENTLY_VIEWED("Recently viewed"),
        FAVORITES("Favorites"),
        SAVED_ITEMS("Saved items"),
        ADD_ACCOUNT("Add account"),
        MANAGE_ACCOUNT("Manage account"),
        CHANGE_ACCOUNT("Change account"),
        SETTINGS("Settings"),
        FEEDBACK("Feedback"),
        ABOUT("About"),
        REPORT("Report"),
        DASHBOARD("Dashboard");

        String mName;

        EventLabel(String name) {
            mName = name;
        }

        public String getValue(){
            return mName;
        }
    }

}
