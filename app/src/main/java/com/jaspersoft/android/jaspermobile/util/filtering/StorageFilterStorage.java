package com.jaspersoft.android.jaspermobile.util.filtering;


import com.jaspersoft.android.jaspermobile.activities.library.LibraryPref_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
@EBean
public class StorageFilterStorage extends FilterStorage {

    @Pref
    protected LibraryPref_ pref;

    @Override
    public String getFilter() {
        return pref.filterType().get();
    }

    @Override
    public void storeFilter(String filterName) {
        pref.filterType().put(filterName);
    }

    @Override
    public void clearFilter() {
        pref.edit().filterType().put(null).apply();
    }
}
