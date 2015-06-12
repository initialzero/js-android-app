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
public class RepositoryResourceFilter extends ResourceFilter {

    private ServerRelease serverRelease;

    @RootContext
    protected FragmentActivity activity;

    private enum RepositoryFilterCategory {
        all(R.string.s_fd_option_all);

        private int mTitleId = -1;

        RepositoryFilterCategory(int titleId) {
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
    }


    @Override
    public String getFilterLocalizedTitle(Filter filter) {
        RepositoryFilterCategory repositoryFilterCategory = RepositoryFilterCategory.valueOf(filter.getName());
        return repositoryFilterCategory.getLocalizedTitle(activity);
    }

    @Override
    protected List<Filter> generateAvailableFilterList() {
        ArrayList<Filter> availableFilters = new ArrayList<>();
        availableFilters.add(getFilterAll());

        return availableFilters;
    }

    @Override
    protected FilterStorage initFilterStorage() {
        return RepositoryFilterStorage_.getInstance_(activity);
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

        return new Filter(RepositoryFilterCategory.all.name(), filterValues);
    }
}
