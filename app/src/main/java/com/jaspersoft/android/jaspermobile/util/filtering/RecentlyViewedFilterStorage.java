package com.jaspersoft.android.jaspermobile.util.filtering;

import org.androidannotations.annotations.EBean;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
@EBean
public class RecentlyViewedFilterStorage extends FilterStorage {

    @Override
    public String getFilter() {
        return null;
    }

    @Override
    public void storeFilter(String filterName) {
    }

    @Override
    public void clearFilter() {
    }
}
