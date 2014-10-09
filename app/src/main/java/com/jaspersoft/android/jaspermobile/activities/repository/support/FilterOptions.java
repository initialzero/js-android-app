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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class FilterOptions {
    public static final ArrayList<String> ALL_REPOSITORY_TYPES = new ArrayList<String>(){{
        add(ResourceLookup.ResourceType.folder.toString());
        add(ResourceLookup.ResourceType.reportUnit.toString());
        add(ResourceLookup.ResourceType.dashboard.toString());
    }};
    public static final ArrayList<String> ALL_LIBRARY_TYPES = new ArrayList<String>(){{
        add(ResourceLookup.ResourceType.reportUnit.toString());
        add(ResourceLookup.ResourceType.dashboard.toString());
    }};
    public static final ArrayList<String> ONLY_DASHBOARD = new ArrayList<String>() {{
        add(ResourceLookup.ResourceType.dashboard.toString());
    }};
    public static final ArrayList<String> ONLY_REPORT = new ArrayList<String>() {{
        add(ResourceLookup.ResourceType.reportUnit.toString());
    }};

    @Pref
    RepositoryPref_ repositoryPref;

    public ArrayList<String> getFilters() {
        Set<String> initialTypes = repositoryPref.filterTypes().get();
        if (initialTypes == null || initialTypes.isEmpty()) {
            putFilters(ALL_LIBRARY_TYPES);
        }
        return Lists.newArrayList(repositoryPref.filterTypes().get());
    }

    public void putFilters(List<String> filters) {
        repositoryPref.filterTypes().put(Sets.newHashSet(filters));
    }
}
