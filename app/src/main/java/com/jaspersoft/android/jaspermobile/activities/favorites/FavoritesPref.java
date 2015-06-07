package com.jaspersoft.android.jaspermobile.activities.favorites;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

import java.util.Set;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface FavoritesPref {
    @DefaultString("LABEL")
    String sortType();

    Set<String> filterTypes();

    String filterType();
}