/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.repository.support;

import android.accounts.Account;
import android.support.v4.app.FragmentActivity;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class FilterManagerBean {

    @RootContext
    protected FragmentActivity activity;
    @Pref
    protected LibraryPref_ pref;

    private ResourceFilterFactory filterFactory;

    @AfterInject
    void initialize() {
        final RoboInjector injector = RoboGuice.getInjector(activity);
        injector.injectMembersWithoutViews(this);

        Account account = JasperAccountManager.get(activity).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(activity, account);
        ServerRelease serverRelease = ServerRelease.parseVersion(accountServerData.getVersionName());

        filterFactory = ResourceFilterFactory.create(serverRelease);

    }

    public ArrayList<String> getFilters() {
        applyDefaultValuesIfNeed();
        return new ArrayList<String>(pref.filterTypes().get());
    }

    public void putFilters(List<String> filters) {
        pref.filterTypes().put(new HashSet<String>(filters));
    }

    public boolean containsOnlyReport() {
        return getFilters().equals(filterFactory.createOnlyReportFilters());
    }

    public boolean containsOnlyDashboard() {
        return getFilters().equals(filterFactory.createOnlyDashboardFilters());
    }

    public ArrayList<String> getReportFilters() {
        return new ArrayList<String>(filterFactory.createOnlyReportFilters());
    }

    public ArrayList<String> getDashboardFilters() {
        return new ArrayList<String>(filterFactory.createOnlyDashboardFilters());
    }

    public ArrayList<String> getFiltersForRepository() {
        return new ArrayList<String>(filterFactory.createFiltersForRepository());
    }

    public ArrayList<String> getFiltersForLibrary() {
        return new ArrayList<String>(filterFactory.createFiltersForLibrary());
    }

    private void applyDefaultValuesIfNeed() {
        Set<String> initialTypes = pref.filterTypes().get();
        if (initialTypes == null || initialTypes.isEmpty()) {
            putFilters(filterFactory.createFiltersForLibrary());
        }
    }

}
