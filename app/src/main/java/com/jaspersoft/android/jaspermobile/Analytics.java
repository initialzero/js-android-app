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

package com.jaspersoft.android.jaspermobile;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.1
 */
public interface Analytics {
    void setScreenName(String screenName);
    void sendScreenView(String screenName, List<Dimension> dimension);
    void sendEvent(String eventCategory, String eventAction, String eventLabel);
    void sendUserChangedEvent();
    void setServerInfo(String serverVersion, String serverEdition);
    void setThumbnailsExist();

    enum ScreenName {
        LIBRARY("Library screen"),
        REPOSITORY("Repository screen"),
        RECENTLY_VIEWED("Recently viewed screen"),
        FAVORITES("Favorites screen"),
        SAVED_ITEMS("Saved items screen"),
        JOBS("Jobs");

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
        RESOURCE("Resource"),
        JOB("Job"),
        CAST("Cast");

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
        SAID_COMMANDS("Said command"),
        VIEWED("Viewed"),
        PRESENTED("Presented"),
        PRESENTATION_STOPPED("Presentation stopped"),
        INFO_VIEWED("Info viewed"),
        SAVED("Saved"),
        REPORT_OPTIONS_VIEWED("Report options viewed"),
        REFRESHED("Refreshed"),
        LOADED_NEXT("Loaded next"),
        SORTED("Sorted"),
        FILTERED("Filtered"),
        CHANGED_VIEW_TYPE("Changed view type"),
        CHANGED("Changed"),
        MARKED_AS_FAVORITE("Marked as favorite"),
        PRINTED("Printed"),
        SHARED("Shared"),
        ANNOTATED("Annotated"),
        ADDED("Added"),
        SELECTED_FOR_SCHEDULE("Selected for schedule"),
        REMOVED("Removed");

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
        JOBS("Jobs"),
        ADD_ACCOUNT("Add account"),
        SWITCH_ACCOUNT("Switch account"),
        SETTINGS("Settings"),
        FEEDBACK("Feedback"),
        ABOUT("About"),
        MANAGE_ACCOUNT("Manage account"),
        REPORT("Report"),
        DASHBOARD("Dashboard"),
        JOB("Job"),
        CHOOSE_REPORT("Choose report"),
        WITH_RO("With report options"),
        VOICE_COMMANDS("Voice commands"),
        RUN("Run"),
        FIND("Find"),
        UNDEFINED("Undefined"),
        DONE("Done"),
        FAILED("Failed"),
        WITH_TEXT("With text"),
        WITH_LINE("With line"),
        CLEARED("Cleared"),
        CANCELED("Canceled");

        String mName;

        EventLabel(String name) {
            mName = name;
        }

        public String getValue() {
            return mName;
        }
    }

    class Dimension {
        public static final int FILTER_TYPE_HIT_KEY = 3;
        public static final int RESOURCE_VIEW_HIT_KEY = 4;

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
