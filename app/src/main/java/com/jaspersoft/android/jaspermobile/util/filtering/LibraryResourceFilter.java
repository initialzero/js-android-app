package com.jaspersoft.android.jaspermobile.util.filtering;

import android.accounts.Account;
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
public class LibraryResourceFilter extends ResourceFilter {
    private ServerRelease serverRelease;
    private boolean isProEdition;

    @RootContext
    protected FragmentActivity activity;

    @AfterInject
    public void initFilter() {
        Account account = JasperAccountManager.get(activity).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(activity, account);
        this.serverRelease = ServerRelease.parseVersion(accountServerData.getVersionName());
        this.isProEdition = accountServerData.getEdition().equals("PRO");
    }

    @Override
    public Filter getDefaultFilter() {
        return getFilterAll();
    }

    @Override
    public List<String> getAvailableFilters() {
        return null;
    }

    @Override
    protected List<Filter> generateAvailableFilterList() {
        ArrayList<Filter> availableFilters = new ArrayList<>();
        // Filtration is not available for CE servers
        if (!isProEdition) return availableFilters;

        availableFilters.add(getFilterAll());
        availableFilters.add(getFilterReport());
        availableFilters.add(getFilterDashboard());

        return availableFilters;
    }

    private Filter getFilterAll(){
        String filterTitle = activity.getString(R.string.s_fd_option_all);
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.addAll(JasperResources.report());
        filterValues.addAll(JasperResources.dashboard(serverRelease));

        return new Filter(filterTitle, filterValues);
    }

    private Filter getFilterReport(){
        String filterTitle = activity.getString(R.string.s_fd_option_reports);
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.addAll(JasperResources.report());

        return new Filter(filterTitle, filterValues);
    }

    private Filter getFilterDashboard(){
        String filterTitle = activity.getString(R.string.s_fd_option_dashboards);
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.addAll(JasperResources.dashboard(serverRelease));

        return new Filter(filterTitle, filterValues);
    }
}
