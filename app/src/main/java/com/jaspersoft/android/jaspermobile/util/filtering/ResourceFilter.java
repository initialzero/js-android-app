package com.jaspersoft.android.jaspermobile.util.filtering;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class ResourceFilter {

    private List<Filter> availableFilters;

    final public Filter get(int position) {
        if (availableFilters == null) {
            availableFilters = generateAvailableFilterList();
        }

        return availableFilters.get(position);
    }

    final public int indexOf(Filter filter) {
        if (availableFilters == null) {
            availableFilters = generateAvailableFilterList();
        }

        return availableFilters.indexOf(filter);
    }

    public abstract Filter getDefaultFilter();
    public abstract List<String> getAvailableFilters();
    protected abstract List<Filter> generateAvailableFilterList();
}
