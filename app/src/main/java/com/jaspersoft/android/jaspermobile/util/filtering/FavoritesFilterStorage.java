package com.jaspersoft.android.jaspermobile.util.filtering;

import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.LibraryPref_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EBean
public class FavoritesFilterStorage extends FilterStorage{
    @Pref
    protected FavoritesPref_ pref;

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
