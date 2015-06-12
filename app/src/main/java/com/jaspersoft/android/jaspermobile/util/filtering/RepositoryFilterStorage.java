package com.jaspersoft.android.jaspermobile.util.filtering;

import org.androidannotations.annotations.EBean;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
@EBean
public class RepositoryFilterStorage extends FilterStorage {

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
