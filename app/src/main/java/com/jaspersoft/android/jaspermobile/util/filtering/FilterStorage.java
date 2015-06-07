package com.jaspersoft.android.jaspermobile.util.filtering;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class FilterStorage {
    public abstract String getFilter();
    public abstract void storeFilter(String selectedFilter);
    public abstract void clearFilter();
}
