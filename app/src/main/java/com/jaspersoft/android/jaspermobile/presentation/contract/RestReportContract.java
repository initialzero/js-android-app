package com.jaspersoft.android.jaspermobile.presentation.contract;

import com.jaspersoft.android.jaspermobile.presentation.page.ReportPageState;
import com.jaspersoft.android.jaspermobile.presentation.view.LoadDataView;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface RestReportContract {
    interface View extends LoadDataView {
        void setFilterActionVisibility(boolean visibilityFlag);

        void setSaveActionVisibility(boolean visibilityFlag);

        void reloadMenu();

        void showInitialFiltersPage();

        void showPage(String pageContent);

        void setPaginationControlVisibility(boolean visibility);

        void resetPaginationControl();

        void showTotalPages(int totalPages);

        void showCurrentPage(int page);

        void showPageOutOfRangeError();

        void showEmptyPageMessage();

        void showReloadMessage();

        void showPageLoader(boolean visibility);

        ReportPageState getState();

        void showWebView(boolean visibility);
    }

    interface Action {
        void loadPage(String pageRange);

        void runReport();

        void updateReport();

        void refresh();
    }
}
