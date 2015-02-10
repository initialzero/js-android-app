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

package com.jaspersoft.android.jaspermobile.activities.repository.support;

import android.support.v4.app.FragmentActivity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.info.ServerInfoSnapshot;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.dashboard;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.folder;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.legacyDashboard;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.reportUnit;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class FilterManager {

    @RootContext
    FragmentActivity activity;
    @Pref
    LibraryPref_ pref;
    @Inject
    ServerInfoSnapshot serverInfo;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(activity);
        injector.injectMembersWithoutViews(this);
    }

    public ArrayList<String> getFilters() {
        Set<String> initialTypes = pref.filterTypes().get();
        if (initialTypes == null || initialTypes.isEmpty()) {
            putFilters(getFiltersByType(Type.ALL_FOR_LIBRARY));
        }
        return Lists.newArrayList(pref.filterTypes().get());
    }

    public void putFilters(List<String> filters) {
        pref.filterTypes().put(Sets.newHashSet(filters));
    }

    public ArrayList<String> getFiltersByType(Type value) {
        if (serverInfo != null && !serverInfo.isMissing() && serverInfo.isAmberRelease()) {
            return value.typesForAmber().getAsList();
        }
        return value.typesForPreAmber().getAsList();
    }

    public boolean isOnlyReport(ArrayList<String> filters) {
        return filters.equals(getFiltersByType(Type.ONLY_REPORT));
    }

    public boolean isOnlyDashboard(ArrayList<String> filters) {
        return filters.equals(getFiltersByType(Type.ONLY_DASHBOARD));
    }

    public static enum Type {
        ALL_FOR_REPOSITORY, ALL_FOR_LIBRARY, ONLY_DASHBOARD, ONLY_REPORT;

        public Filter typesForAmber() {
            switch (this) {
                case ALL_FOR_REPOSITORY:
                    return Filter.ALL_AMBER;
                case ALL_FOR_LIBRARY:
                    return Filter.ALL_LIBRARY_AMBER;
                case ONLY_DASHBOARD:
                    return Filter.ONLY_DASHBOARD_AMBER;
                case ONLY_REPORT:
                    return Filter.ONLY_REPORT_TYPES;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        public Filter typesForPreAmber() {
            switch (this) {
                case ALL_FOR_REPOSITORY:
                    return Filter.ALL_PRE_AMBER;
                case ALL_FOR_LIBRARY:
                    return Filter.ALL_LIBRARY_PRE_AMBER;
                case ONLY_DASHBOARD:
                    return Filter.ONLY_DASHBOARD_PRE_AMBER;
                case ONLY_REPORT:
                    return Filter.ONLY_REPORT_TYPES;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    private static enum Filter {
        ALL_PRE_AMBER(folder, reportUnit, dashboard),
        ALL_AMBER(folder, reportUnit, legacyDashboard),
        ALL_LIBRARY_PRE_AMBER(reportUnit, dashboard),
        ALL_LIBRARY_AMBER(reportUnit, legacyDashboard),
        ONLY_DASHBOARD_PRE_AMBER(dashboard),
        ONLY_DASHBOARD_AMBER(legacyDashboard),
        ONLY_REPORT_TYPES(reportUnit);

        private final ArrayList<String> mTypes = new ArrayList<String>();

        Filter(ResourceLookup.ResourceType... types) {
            for (ResourceLookup.ResourceType type : types) {
                mTypes.add(type.toString());
            }
        }

        public ArrayList<String> getAsList() {
            return mTypes;
        }
    }
}
