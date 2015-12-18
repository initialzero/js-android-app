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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.interactor.GetReportControlsCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.GetReportPageCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.GetReportTotalPagesCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.IsReportMultiPageCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.ReloadReportCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.RunReportExecutionCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.UpdateReportExecutionCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportView;
import com.jaspersoft.android.jaspermobile.util.rx.DefaultSubscriber;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;

import java.util.List;

import rx.Subscriber;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ReportViewPresenter implements ReportActionListener, Presenter {

    private static final String FIRST_PAGE = "1";

    private final GetReportPageCase mGetReportPageCase;
    private final GetReportControlsCase mGetReportControlsCase;
    private final GetReportTotalPagesCase mGetReportTotalPagesCase;
    private final IsReportMultiPageCase mIsReportMultiPageCase;
    private final RunReportExecutionCase mRunReportExecutionCase;
    private final UpdateReportExecutionCase mUpdateReportExecutionCase;
    private final ReloadReportCase mReloadReportCase;

    private RequestExceptionHandler mExceptionHandler;
    private ReportView mView;
    private String mCurrentPage;

    public ReportViewPresenter(
            RequestExceptionHandler exceptionHandler,
            GetReportControlsCase getReportControlsCase,
            GetReportPageCase getReportPageCase,
            GetReportTotalPagesCase getReportTotalPagesCase,
            IsReportMultiPageCase isReportMultiPageCase,
            RunReportExecutionCase runReportExecutionCase,
            UpdateReportExecutionCase updateReportExecutionCase,
            ReloadReportCase reloadReportCase) {
        mExceptionHandler = exceptionHandler;
        mGetReportPageCase = getReportPageCase;
        mGetReportControlsCase = getReportControlsCase;
        mGetReportTotalPagesCase = getReportTotalPagesCase;
        mIsReportMultiPageCase = isReportMultiPageCase;
        mRunReportExecutionCase = runReportExecutionCase;
        mUpdateReportExecutionCase = updateReportExecutionCase;
        mReloadReportCase = reloadReportCase;
    }

    public void setView(ReportView view) {
        mView = view;
    }

    public void init() {
        if (mCurrentPage == null) {
            mView.showLoading();
            loadInputControls();
        } else {
            loadCurrentPage();
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

    @Override
    public void refresh() {
        mView.showCurrentPage(1);
        reloadReport(FIRST_PAGE);
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
        mReloadReportCase.unsubscribe();
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

    private void showErrorMessage(Throwable error) {
        mView.hideLoading();
        mView.showError(mExceptionHandler.extractMessage(error));
    }

    private void handleErrorForPage(@Nullable String pagePosition, @NonNull Throwable e) {
        if (pagePosition == null) {
            pagePosition = FIRST_PAGE;
        }

        if (e instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) e;
            int errorCode = serviceException.code();
            switch (errorCode) {
                case StatusCodes.EXPORT_EXECUTION_FAILED:
                    mView.showPageOutOfRangeError();
                    loadCurrentPage();
                    break;
                case StatusCodes.REPORT_EXECUTION_INVALID:
                    mView.showLoading();
                    mView.showReloadMessage();
                    reloadReport(pagePosition);
                    break;
                default:
                    Timber.e(e, "Page request operation crashed with SDK exception");
                    showErrorMessage(e);
            }
        } else {
            Timber.e(e, "Page request operation crashed");
            showErrorMessage(e);
        }
    }

    private void loadCurrentPage() {
        if (mCurrentPage != null) {
            loadPage(mCurrentPage);
        }
    }

    private void reloadReport(String page) {
        mView.resetPaginationControl();
        mReloadReportCase.execute(new ReloadReportListener(page));
    }

    private void onControlsLoaded(List<InputControl> controls) {
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

    private void onRunStarted() {
        mView.hideError();
        checkIsMultiPageReport();
        loadPage(FIRST_PAGE);
    }

    private void onPageLoaded(String pagePosition, ReportPage page) {
        mCurrentPage = pagePosition;
        mView.hideError();
        mView.showCurrentPage(Integer.valueOf(pagePosition));
        mView.showPage(page.getContent());
    }

    private void onMultiPageCheckFinished(Boolean isMultiPage) {
        if (isMultiPage) {
            mView.showPaginationControl();
            loadTotalPagesCount();
        } else {
            mView.setSaveActionVisibility(true);
            mView.reloadMenu();
        }
    }

    private void onTotalPagesLoaded(Integer totalPages) {
        boolean hasNoPages = (totalPages == 0);
        if (hasNoPages) {
            mView.setSaveActionVisibility(false);
            mView.showEmptyPageMessage();
            mView.hidePaginationControl();
        } else {
            mView.setSaveActionVisibility(true);
            mView.showTotalPages(totalPages);
            loadCurrentPage();

            boolean hasMoreThanOnePage = (totalPages > 1);
            if (hasMoreThanOnePage) {
                mView.showPaginationControl();
            } else {
                mView.hidePaginationControl();
            }
        }
        mView.reloadMenu();
    }

    private void onExecutionUpdated() {
        mView.resetPaginationControl();
        checkIsMultiPageReport();
        loadPage(FIRST_PAGE);
    }

    private void onReloadFinished(String pagePosition) {
        mView.hideError();
        checkIsMultiPageReport();
        loadPage(pagePosition);
    }

    private class InputControlsListener extends DefaultSubscriber<List<InputControl>> {
        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Request for input controls crashed");
            showErrorMessage(e);
        }

        @Override
        public void onNext(List<InputControl> controls) {
            onControlsLoaded(controls);
        }
    }

    private class RunReportListener extends DefaultSubscriber<Void> {
        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Run report operation crashed");
            handleErrorForPage(mCurrentPage, e);
        }

        @Override
        public void onNext(Void aVoid) {
            onRunStarted();
        }
    }

    private class PageResultListener extends Subscriber<ReportPage> {
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
            Timber.e(e, "Page request operation crashed");
            handleErrorForPage(pagePosition, e);
        }

        @Override
        public void onNext(ReportPage page) {
            onPageLoaded(pagePosition, page);
        }
    }

    private class IsMultiPageListener extends DefaultSubscriber<Boolean> {
        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Is multi page request operation crashed");
            handleErrorForPage(mCurrentPage, e);
        }

        @Override
        public void onNext(Boolean isMultiPage) {
            onMultiPageCheckFinished(isMultiPage);
        }
    }

    private class TotalPagesListener extends DefaultSubscriber<Integer> {
        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Total pages request operation crashed");
            handleErrorForPage(mCurrentPage, e);
        }

        @Override
        public void onNext(Integer totalPages) {
            onTotalPagesLoaded(totalPages);
        }
    }

    private class UpdateExecutionListener extends DefaultSubscriber<Void> {
        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Update execution operation crashed");
            handleErrorForPage(mCurrentPage, e);
        }

        @Override
        public void onNext(Void Void) {
            onExecutionUpdated();
        }
    }

    private class ReloadReportListener extends DefaultSubscriber<Void> {
        private final String mPage;

        public ReloadReportListener(String page) {
            mPage = page;
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Failed to reload report");
            showErrorMessage(e);
        }

        @Override
        public void onNext(Void aVoid) {
            onReloadFinished(mPage);
        }
    }
}
