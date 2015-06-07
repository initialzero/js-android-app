package com.jaspersoft.android.jaspermobile.activities.storage;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

import java.util.Set;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface StoragePref {
    @DefaultString("LABEL")
    String sortType();

    Set<String> filterTypes();

    String filterType();
}