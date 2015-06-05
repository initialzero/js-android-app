package com.jaspersoft.android.jaspermobile.util.filtering;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class LibraryFilterStorage implements FilterStorage {

    private Filter currentFilter;

    public LibraryFilterStorage(ResourceFilter resourceFilter) {
        this.currentFilter = resourceFilter.getDefaultFilter();
    }

    @Override
    public Filter getFilter() {
        return currentFilter;
    }

    @Override
    public void storeFilter(Filter selectedFilter) {
        currentFilter = selectedFilter;
    }
}
