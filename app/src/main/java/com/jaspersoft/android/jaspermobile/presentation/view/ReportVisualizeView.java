package com.jaspersoft.android.jaspermobile.presentation.view;

import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeViewModel;
import com.jaspersoft.android.jaspermobile.presentation.page.ReportPageState;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportVisualizeView extends LoadDataView {
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

    void loadTemplateInView(VisualizeTemplate template);

    ReportPageState getState();

    VisualizeViewModel getVisualize();
}
