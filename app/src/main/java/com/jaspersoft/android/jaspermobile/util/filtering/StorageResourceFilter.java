package com.jaspersoft.android.jaspermobile.util.filtering;

import android.accounts.Account;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
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
public class StorageResourceFilter extends ResourceFilter {


    @RootContext
    protected FragmentActivity activity;

    private enum StorageFilterCategory {
        all(R.string.si_fd_option_all),
        html(R.string.si_fd_option_html),
        pdf(R.string.si_fd_option_pdf),
        xls(R.string.si_fd_option_xls);

        private int mTitleId = -1;

        StorageFilterCategory(int titleId) {
            mTitleId = titleId;
        }

        public String getLocalizedTitle(Context context) {
            return context.getString(this.mTitleId);
        }
    }

    @Override
    public String getFilterLocalizedTitle(Filter filter) {
        StorageFilterCategory storageFilterCategory = StorageFilterCategory.valueOf(filter.getName());
        return storageFilterCategory.getLocalizedTitle(activity);
    }

    @Override
    protected List<Filter> generateAvailableFilterList() {
        ArrayList<Filter> availableFilters = new ArrayList<>();

        availableFilters.add(getFilterAll());
        availableFilters.add(getFilterHtml());
        availableFilters.add(getFilterPdf());
        availableFilters.add(getFilterXls());

        return availableFilters;
    }

    @Override
    protected FilterStorage initFilterStorage() {
        return LibraryFilterStorage_.getInstance_(activity);
    }

    @Override
    protected Filter getDefaultFilter() {
        return getFilterAll();
    }

    private Filter getFilterAll(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileAdapter.FileType.HTML.toString());
        filterValues.add(FileAdapter.FileType.PDF.toString());
        filterValues.add(FileAdapter.FileType.XLS.toString());

        return new Filter(StorageFilterCategory.all.name(), filterValues);
    }

    private Filter getFilterHtml(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileAdapter.FileType.HTML.toString());

        return new Filter(StorageFilterCategory.html.name(), filterValues);
    }

    private Filter getFilterPdf(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileAdapter.FileType.PDF.toString());

        return new Filter(StorageFilterCategory.pdf.name(), filterValues);
    }

    private Filter getFilterXls(){
        ArrayList<String> filterValues = new ArrayList<>();
        filterValues.add(FileAdapter.FileType.XLS.toString());

        return new Filter(StorageFilterCategory.xls.name(), filterValues);
    }
}
