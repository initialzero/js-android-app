/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.domain.interactor.GetReportControlsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.GetReportPageCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.GetReportTotalPagesCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.IsReportMultiPageCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.RunReportExecutionCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.UpdateReportExecutionCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportView;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ReportViewPresenter implements ReportActionListener, Presenter {

    private final GetReportPageCase mGetReportPageCase;
    private final GetReportControlsCase mGetReportControlsCase;
    private final GetReportTotalPagesCase mGetReportTotalPagesCase;
    private final IsReportMultiPageCase mIsReportMultiPageCase;
    private final RunReportExecutionCase mRunReportExecutionCase;
    private final UpdateReportExecutionCase mUpdateReportExecutionCase;

    private RequestExceptionHandler mExceptionHandler;
    private ReportView mView;

    public ReportViewPresenter(
            RequestExceptionHandler exceptionHandler,
            GetReportControlsCase getReportControlsCase,
            GetReportPageCase getReportPageCase,
            GetReportTotalPagesCase getReportTotalPagesCase,
            IsReportMultiPageCase isReportMultiPageCase,
            RunReportExecutionCase runReportExecutionCase,
            UpdateReportExecutionCase updateReportExecutionCase) {
        mExceptionHandler = exceptionHandler;
        mGetReportPageCase = getReportPageCase;
        mGetReportControlsCase = getReportControlsCase;
        mGetReportTotalPagesCase = getReportTotalPagesCase;
        mIsReportMultiPageCase = isReportMultiPageCase;
        mRunReportExecutionCase = runReportExecutionCase;
        mUpdateReportExecutionCase = updateReportExecutionCase;
    }

    public void setView(ReportView view) {
        mView = view;
    }

    public void init(String page) {
        if (page == null) {
            mView.showLoading();
            loadInputControls();
        } else {
            loadPage(page);
            checkIsMultiPageReport();
        }
    }

    @Override
    public void loadPage(String pageRange) {
        mView.showLoading();
        mGetReportPageCase.setPageRange(pageRange);
        mGetReportPageCase.execute(new PageResultListener(pageRange));
    }

    @Override
    public void runReport() {
        mView.setSaveActionVisibility(false);
        mView.reloadMenu();

        mView.showLoading();
        mRunReportExecutionCase.execute(new RunReportListener());
    }

    @Override
    public void updateReport() {
        mUpdateReportExecutionCase.execute(new UpdateExecutionListener());
    }

    private void loadInputControls() {
        mGetReportControlsCase.execute(new InputControlsListener());
    }

    private void checkIsMultiPageReport() {
        mIsReportMultiPageCase.execute(new IsMultiPageListener());
    }

    private void loadTotalPagesCount() {
        mGetReportTotalPagesCase.execute(new TotalPagesListener());
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        mGetReportPageCase.unsubscribe();
        mGetReportControlsCase.unsubscribe();
        mGetReportTotalPagesCase.unsubscribe();
        mIsReportMultiPageCase.unsubscribe();
        mRunReportExecutionCase.unsubscribe();
        mUpdateReportExecutionCase.unsubscribe();
    }

    private void showErrorMessage(Throwable error) {
        mView.hideLoading();
        mView.showError(mExceptionHandler.extractMessage(error));
    }

    private class InputControlsListener extends Subscriber<List<InputControl>> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(List<InputControl> controls) {
            boolean showFilterActionVisible = !controls.isEmpty();

            mView.hideError();
            mView.setFilterActionVisibility(showFilterActionVisible);
            mView.reloadMenu();

            if (showFilterActionVisible) {
                mView.hideLoading();
                mView.showInitialFiltersPage();
            } else {
                runReport();
            }
        }
    }

    private class RunReportListener extends Subscriber<Void> {
        @Override
        public void onCompleted() {
            mView.hideLoading();
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(Void aVoid) {
            mView.hideError();
            checkIsMultiPageReport();
            loadPage("1");
        }
    }

    private class PageResultListener extends Subscriber<String> {
        private final String pagePosition;

        private PageResultListener(String pagePosition) {
            this.pagePosition = pagePosition;
        }

        @Override
        public void onCompleted() {
            mView.hideLoading();
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(String pageContent) {
            mView.hideError();
            mView.showPage(pagePosition, pageContent);
        }
    }

    private class IsMultiPageListener extends Subscriber<Boolean> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(Boolean isMultiPage) {
            if (isMultiPage) {
                mView.showPaginationControl();
                loadTotalPagesCount();
            } else {
                mView.setSaveActionVisibility(true);
                mView.reloadMenu();
            }
        }
    }

    private class TotalPagesListener extends Subscriber<Integer> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(Integer totalPages) {
            mView.setSaveActionVisibility(true);
            mView.reloadMenu();
            mView.showTotalPages(totalPages);
        }
    }

    private class UpdateExecutionListener extends Subscriber<Void> {
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(Void Void) {
            mView.resetPaginationControl();
            checkIsMultiPageReport();
            loadPage("1");
        }
    }
}
