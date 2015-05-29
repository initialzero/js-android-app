/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.dashboard;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.folder;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.legacyDashboard;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.reportUnit;

/**
 * Encapsulates business logic of filtering options across different JRS releases.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class ResourceFilterFactory {

    private final ServerRelease serverRelease;

    private ResourceFilterFactory(ServerRelease serverRelease) {
        this.serverRelease = serverRelease;
    }

    public static ResourceFilterFactory create(ServerRelease release) {
        return new ResourceFilterFactory(release);
    }

    public List<String> createFiltersForLibrary() {
        return getFiltersByType(Type.ALL_FOR_LIBRARY);
    }

    public List<String> createFiltersForRepository() {
        return getFiltersByType(Type.ALL_FOR_REPOSITORY);
    }

    public List<String> createOnlyReportFilters() {
        return getFiltersByType(Type.ONLY_REPORT);
    }

    public List<String> createOnlyDashboardFilters() {
        return getFiltersByType(Type.ONLY_DASHBOARD);
    }

    private List<String> getFiltersByType(Type value) {
        if (serverRelease.code() >= ServerRelease.AMBER.code()) {
            return value.typesForAmber().getAsList();
        } else {
            return value.typesForPreAmber().getAsList();
        }
    }

    private static enum Type {
        ALL_FOR_REPOSITORY, ALL_FOR_LIBRARY, ONLY_DASHBOARD, ONLY_REPORT;

        Filter typesForAmber() {
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

        Filter typesForPreAmber() {
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
        ALL_AMBER(folder, reportUnit, legacyDashboard, dashboard),
        ALL_LIBRARY_PRE_AMBER(reportUnit, dashboard),
        ALL_LIBRARY_AMBER(reportUnit, legacyDashboard, dashboard),
        ONLY_DASHBOARD_PRE_AMBER(dashboard),
        ONLY_DASHBOARD_AMBER(legacyDashboard, dashboard),
        ONLY_REPORT_TYPES(reportUnit);

        private final ArrayList<String> mTypes = new ArrayList<String>();

        Filter(ResourceLookup.ResourceType... types) {
            for (ResourceLookup.ResourceType type : types) {
                mTypes.add(type.toString());
            }
        }

        ArrayList<String> getAsList() {
            return mTypes;
        }
    }
}
