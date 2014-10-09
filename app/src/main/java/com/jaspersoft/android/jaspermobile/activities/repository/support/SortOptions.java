package com.jaspersoft.android.jaspermobile.activities.repository.support;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean(scope = EBean.Scope.Singleton)
public class SortOptions {

    @Pref
    RepositoryPref_ repositoryPref;

    public SortOrder getOrder() {
        return SortOrder.valueOf(repositoryPref.sortType().get());
    }

    public void putOrder(SortOrder sortOrder) {
        repositoryPref.sortType().put(sortOrder.toString());
    }

}
