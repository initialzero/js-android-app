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

import android.graphics.Bitmap;
import android.net.Uri;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ScreenCapture;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.ui.model.visualize.VisualizeViewModel;
import com.jaspersoft.android.jaspermobile.ui.page.ReportPageState;
import com.jaspersoft.android.jaspermobile.ui.view.LoadDataView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.io.File;
import java.net.URI;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface VisualizeReportContract {
    interface View extends LoadDataView {
        void showFilterAction(boolean visibilityFlag);

        void showSaveAction(boolean visibilityFlag);

        void showProgress();

        void reloadMenu();

        void showInitialFiltersPage();

        void showPageOutOfRangeError();

        void showPagination(boolean visibility);

        void setPaginationEnabled(boolean enabled);

        void setPaginationCurrentPage(int page);

        void setPaginationTotalPages(int totalPages);

        int getPaginationTotalPages();

        void resetPaginationControl();


        void showWebView(boolean visibility);

        void showReloadButton(boolean visibility);


        void showEmptyPageMessage();

        void hideEmptyPageMessage();


        void loadTemplateInView(VisualizeTemplate template);

        void updateDeterminateProgress(int progress);

        void showExternalLink(String externalLink);

        void executeReport(ResourceLookup reportModel);

        void resetZoom();

        ReportPageState getState();

        VisualizeViewModel getVisualize();

        void handleSessionExpiration();

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
