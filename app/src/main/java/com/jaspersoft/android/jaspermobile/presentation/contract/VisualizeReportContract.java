package com.jaspersoft.android.jaspermobile.presentation.contract;

import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.presentation.model.ReportResourceModel;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeViewModel;
import com.jaspersoft.android.jaspermobile.presentation.page.ReportPageState;
import com.jaspersoft.android.jaspermobile.presentation.view.LoadDataView;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface VisualizeReportContract {
    interface View extends LoadDataView {
        void setFilterActionVisibility(boolean visibilityFlag);

        void setSaveActionVisibility(boolean visibilityFlag);

        void reloadMenu();

        void showInitialFiltersPage();

        void showPageOutOfRangeError();

        void setPaginationVisibility(boolean visibility);

        void setPaginationEnabled(boolean enabled);

        void setPaginationCurrentPage(int page);

        void setPaginationTotalPages(int totalPages);

        int getPaginationTotalPages();

        void resetPaginationControl();


        void setWebViewVisibility(boolean visibility);


        void showEmptyPageMessage();

        void hideEmptyPageMessage();


        void loadTemplateInView(VisualizeTemplate template);

        void updateDeterminateProgress(int progress);

        void showExternalLink(String externalLink);

        void executeReport(ReportResourceModel reportModel);

        void resetZoom();

        ReportPageState getState();

        VisualizeViewModel getVisualize();

        void handleSessionExpiration();
    }

    interface Action {
        void loadPage(String pageRange);

        void runReport();

        void updateReport();

        void refresh();
    }
}
