package com.jaspersoft.android.jaspermobile.util.filtering;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public interface FilterStorage {
    Filter getFilter();
    void storeFilter(Filter selectedFilter);
}
