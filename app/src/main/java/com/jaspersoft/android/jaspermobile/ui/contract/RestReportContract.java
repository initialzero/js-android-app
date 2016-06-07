/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.ui.contract;

import com.jaspersoft.android.jaspermobile.domain.ScreenCapture;
import com.jaspersoft.android.jaspermobile.ui.model.visualize.VisualizeViewModel;
import com.jaspersoft.android.jaspermobile.ui.page.ReportPageState;
import com.jaspersoft.android.jaspermobile.ui.view.LoadDataView;

import java.io.File;

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

        void showPaginationControl(boolean visibility);

        void resetPaginationControl();

        int getPaginationTotalPages();

        void showTotalPages(int totalPages);

        void showCurrentPage(int page);

        void showPageOutOfRangeError();

        void showEmptyPageMessage();

        void showReloadMessage();

        void showProgress();

        void showPageLoader(boolean visibility);

        ReportPageState getState();

        void showWebView(boolean visibility);

        VisualizeViewModel getVisualize();

        void navigateToAnnotationPage(File file);
    }

    interface Action {
        void loadPage(String pageRange);

        void runReport();

        void updateReport();

        void refresh();

        void shareReport(ScreenCapture screenCapture);
    }
}
