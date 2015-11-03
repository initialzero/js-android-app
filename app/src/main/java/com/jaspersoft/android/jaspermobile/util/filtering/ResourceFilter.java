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

package com.jaspersoft.android.jaspermobile.util.filtering;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class ResourceFilter {

    private FilterStorage filterStorage;
    private List<Filter> availableFilters;

    final public Filter getCurrent() {
        Filter newFilter = getFilterByName(getFilterStorage().getFilter());
        if (newFilter == null) {
            newFilter = getDefaultFilter();
        }
        return newFilter;
    }

    final public void persist(Filter filter) {
        getFilterStorage().storeFilter(filter.getName());
    }

    final public int getPosition() {
        return getAvailableFilters().indexOf(getCurrent());
    }

    final public Filter get(int position) {
        return getAvailableFilters().get(position);
    }

    final public List<String> getFilters() {
        List<String> availableFilterTitles = new ArrayList<>();
        for (Filter filter : getAvailableFilters()) {
            availableFilterTitles.add(getFilterLocalizedTitle(filter));
        }
        return availableFilterTitles;
    }

    final protected List<Filter> getAvailableFilters() {
        if (availableFilters == null) {
            availableFilters = generateAvailableFilterList();
        }
        return availableFilters;
    }

    final protected FilterStorage getFilterStorage() {
        if (filterStorage == null) {
            filterStorage = initFilterStorage();
        }
        return filterStorage;
    }

    final protected Filter getFilterByName(String name) {
        for (Filter filter : getAvailableFilters()) {
            if (filter.getName().equals(name)) return filter;
        }
        return null;
    }

    protected abstract String getFilterLocalizedTitle(Filter filter);

    protected abstract List<Filter> generateAvailableFilterList();

    protected abstract FilterStorage initFilterStorage();

    protected abstract Filter getDefaultFilter();
}
