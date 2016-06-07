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

package com.jaspersoft.android.jaspermobile.ui.presenter;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.ui.component.presenter.BasePresenter;
import com.jaspersoft.android.jaspermobile.ui.contract.ScheduleFormContract;
import com.jaspersoft.android.jaspermobile.ui.entity.job.JobFormViewBundle;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@PerActivity
public class ScheduleFormPresenter extends BasePresenter<ScheduleFormContract.View>
        implements ScheduleFormContract.EventListener, ScheduleFormContract.Model.Callback {
    private final ScheduleFormContract.Model mModel;
    private final RequestExceptionHandler mExceptionHandler;

    @Inject
    public ScheduleFormPresenter(ScheduleFormContract.Model model, RequestExceptionHandler exceptionHandler) {
        mModel = model;
        mExceptionHandler = exceptionHandler;
    }

    @Override
    public void onBindView(ScheduleFormContract.View view) {
        mModel.bind(this);
    }

    @Override
    public void onViewReady() {
        mView.showFormLoadingMessage();
        mModel.load();
    }

    @Override
    public void onSubmitClick(JobFormViewBundle form) {
        mView.showSubmitMessage();
        mModel.submit(form);
    }

    @Override
    public void onFormLoadSuccess(JobFormViewBundle form) {
        mView.hideFormLoadingMessage();
        mView.showForm(form);
    }

    @Override
    public void onFormLoadError(Throwable error) {
        mView.hideFormLoadingMessage();
        handleError(error);
    }

    @Override
    public void onFormSubmitSuccess() {
        mView.showSubmitSuccess();
        mView.hideFormLoadingMessage();
    }

    public void onFormSubmitError(Throwable error) {
        mView.hideSubmitMessage();
        handleError(error);
    }

    private void handleError(Throwable error) {
        mExceptionHandler.showAuthErrorIfExists(error);
        Timber.e(error, "ScheduleFormPresenter messaged!");
    }
}
