package com.jaspersoft.android.jaspermobile.util.filtering;

import android.accounts.Account;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
@EBean
public class FavoritesResourceFilter extends ResourceFilter {

    private ServerRelease serverRelease;
    private boolean isProEdition;

    @RootContext
    protected FragmentActivity activity;

    private enum FavoritesFilterCategory {
        all(R.string.s_fd_option_all),
        reports(R.string.s_fd_option_reports),
        dashboards(R.string.s_fd_option_dashboards),
        folders(R.string.f_fd_option_folders);

        private int mTitleId = -1;

        FavoritesFilterCategory(int titleId) {
            mTitleId = titleId;
        }

        public String getLocalizedTitle(Context context) {
            return context.getString(this.mTitleId);
        }
    }

    @AfterInject
    protected void initFilter() {
        Account account = JasperAccountManager.get(activity).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(activity, account);
        this.serverRelease = ServerRelease.parseVersion(accountServerData.getVersionName());
        this.isProEdition = accountServerData.getEdition().equals("PRO");
    }

    @Override
    protected String getFilterLocalizedTitle(Filter filter) {
        FavoritesFilterCategory favoritesFilterCategory = FavoritesFilterCategory.valueOf(filter.getName());
        return favoritesFilterCategory.getLocalizedTitle(activity);
    }

    @Override
    protected List<Filter> generateAvailableFilterList() {
        ArrayList<Filter> availableFilters = new ArrayList<>();

        availableFilters.add(getFilterAll());
        availableFilters.add(getFilterReport());
        if (isProEdition) {
            availableFilters.add(getFilterDashboard());
        }
        availableFilters.add(getFilterFolder());

        return availableFilters;
    }

    @Override
    protected FilterStorage initFilterStorage() {
        return FavoritesFilterStorage_.getInstance_(activity);
    }

    @Override
    protected Filter getDefaultFilter() {
        return getFilterAll();
    }

    private Filter getFilterAll() {
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.addAll(JasperResources.report());
        filterValues.addAll(JasperResources.dashboard(serverRelease));
        filterValues.addAll(JasperResources.folder());

        return new Filter(FavoritesFilterCategory.all.name(), filterValues);
    }

    private Filter getFilterReport() {
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.addAll(JasperResources.report());

        return new Filter(FavoritesFilterCategory.reports.name(), filterValues);
    }

    private Filter getFilterDashboard() {
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.addAll(JasperResources.dashboard(serverRelease));

        return new Filter(FavoritesFilterCategory.dashboards.name(), filterValues);
    }

    private Filter getFilterFolder() {
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.addAll(JasperResources.folder());

        return new Filter(FavoritesFilterCategory.folders.name(), filterValues);
    }
}
