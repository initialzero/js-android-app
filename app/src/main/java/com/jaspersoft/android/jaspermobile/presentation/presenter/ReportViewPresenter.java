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

import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.model.ReportModel;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportView;
import com.jaspersoft.android.jaspermobile.util.RxTransformer;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.service.report.ReportExecution;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ReportViewPresenter implements ReportActionListener, Presenter {

    private final ReportModel mReportModel;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private RequestExceptionHandler mExceptionHandler;
    private ReportView mView;

    public ReportViewPresenter(
            RequestExceptionHandler exceptionHandler,
            ReportModel reportModel) {
        mExceptionHandler = exceptionHandler;
        mReportModel = reportModel;
    }

    public void setView(ReportView view) {
        mView = view;
    }

    public void init() {
        mView.showLoading();
        loadInputControls();
    }

    @Override
    public void loadPage(int page) {
        mView.showLoading();
        loadExport(page, new DownloadExportListener());
    }

    @Override
    public void runReport() {
        mView.setSaveActionVisibility(false);
        mView.setFilterActionVisibility(false);
        mView.reloadMenu();

        Subscription subscription = mReportModel.runReport()
                .compose(RxTransformer.<ReportExecution>applySchedulers())
                .subscribe(new RunReportListener());
        mCompositeSubscription.add(subscription);
    }

    @Override
    public void updateReport() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int getPageCount() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void loadInputControls() {
        Subscription subscription = mReportModel.loadInputControls()
                .compose(RxTransformer.<InputControlsList>applySchedulers())
                .subscribe(new InputControlsListener());
        mCompositeSubscription.add(subscription);
    }

    private void loadExport(int page, Subscriber<String> listener) {
        Subscription subscription = mReportModel.downloadExport(page)
                .compose(RxTransformer.<String>applySchedulers())
                .subscribe(listener);
        mCompositeSubscription.add(subscription);
    }

    private void checkIsMultiPageReport() {
        loadExport(2, new IsMultiPageListener());
    }

    private void loadTotalPagesCount() {
        Subscription subscription = mReportModel.loadTotalPages()
                .compose(RxTransformer.<Integer>applySchedulers())
                .subscribe(new TotalPagesListener());
        mCompositeSubscription.add(subscription);
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        mCompositeSubscription.unsubscribe();
    }

    private void showErrorMessage(Throwable error) {
        mView.hideLoading();
        mView.showError(mExceptionHandler.extractMessage(error));
    }

    private class InputControlsListener extends Subscriber<InputControlsList> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(InputControlsList controlsList) {
            List<InputControl> icList = new ArrayList<>(controlsList.getInputControls());
            boolean showFilterActionVisible = !icList.isEmpty();

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

    private class RunReportListener extends Subscriber<ReportExecution> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(ReportExecution reportExecution) {
            mView.hideError();
            loadPage(1);
            checkIsMultiPageReport();
        }
    }

    private class DownloadExportListener extends Subscriber<String> {
        @Override
        public void onCompleted() {
            mView.hideLoading();
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(String page) {
            mView.hideError();
            mView.showPage(page);
        }
    }

    private class IsMultiPageListener extends Subscriber<String> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e);
        }

        @Override
        public void onNext(String s) {
            mView.showPaginationControl();
            loadTotalPagesCount();
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
            mView.showTotalPages(totalPages);
        }
    }
}
