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
