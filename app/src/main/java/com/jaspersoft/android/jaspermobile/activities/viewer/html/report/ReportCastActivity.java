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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity_;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.PageDialogFragment;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.CastActivity;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.cast.ResourcePresentationService;
import com.jaspersoft.android.jaspermobile.widget.AbstractPaginationView;
import com.jaspersoft.android.jaspermobile.widget.PaginationBarView;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import rx.Subscriber;
import timber.log.Timber;


/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@OptionsMenu({R.menu.webview_menu, R.menu.report_filter_manager_menu})
@EActivity(R.layout.activity_cast_report)
public class ReportCastActivity extends CastActivity
        implements
        ReportView,
        ResourcePresentationService.ResourcePresentationCallback,
        NumberDialogFragment.NumberDialogClickListener,
        PageDialogFragment.PageDialogClickListener,
        AbstractPaginationView.OnPageChangeListener,
        AbstractPaginationView.OnPickerSelectedListener
{

    private static final int REQUEST_INITIAL_REPORT_PARAMETERS = 100;
    private static final int REQUEST_NEW_REPORT_PARAMETERS = 200;

    @Extra
    protected ResourceLookup resource;

    @ViewById(R.id.progressLoading)
    protected ProgressBar reportProgress;

    @ViewById(R.id.reportMessage)
    protected TextView reportMessage;

    @ViewById(R.id.scrollContainer)
    protected LinearLayout scrollContainer;

    @ViewById(R.id.paginationControl)
    protected PaginationBarView paginationBar;

    @OptionsMenuItem(R.id.refreshAction)
    protected MenuItem refreshAction;

    @OptionsMenuItem(R.id.showFilters)
    protected MenuItem showFilters;

    @InstanceState
    protected Boolean mHasControls;

    @Inject
    protected ReportParamsStorage paramsStorage;
    @Inject
    protected GetReportShowControlsPropertyCase mGetReportShowControlsPropertyCase;
    @Inject
    protected ReportParamsMapper mReportParamsMapper;

    private ResourcePresentationService mResourcePresentationService;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getProfileComponent().inject(this);

        mResourcePresentationService = (ResourcePresentationService) ResourcePresentationService.getInstance();
        mTimer = new Timer();
    }

    @AfterViews
    protected void run() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(resource.getLabel());
        }
        paginationBar.setOnPageChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!ResourcePresentationService.isStarted()) {
            finish();
        } else {
            mResourcePresentationService.addResourcePresentationCallback(this);
            mResourcePresentationService.synchronizeState(resource, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mResourcePresentationService.removeResourcePresentationCallback(this);
        cancelScrolling();
    }

    @Override
    protected void onDestroy() {
        mGetReportShowControlsPropertyCase.unsubscribe();
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isPresenting = mResourcePresentationService.isPresenting();

        refreshAction.setVisible(isPresenting);
        showFilters.setVisible(isPresenting && !getInputControls().isEmpty());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_rc_s);
    }

    @OptionsItem
    public boolean showFilters() {
        InputControlsActivity_.intent(ReportCastActivity.this)
                .reportUri(resource.getUri())
                .startForResult(REQUEST_NEW_REPORT_PARAMETERS);
        return true;
    }

    @OptionsItem
    public void refreshAction() {
        mResourcePresentationService.refresh();
    }

    @OnActivityResult(REQUEST_INITIAL_REPORT_PARAMETERS)
    final void onInitialsParametersResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            requestReportCasting();
        } else {
            finish();
        }
    }

    @OnActivityResult(REQUEST_NEW_REPORT_PARAMETERS)
    final void onNewParametersResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            boolean isNewParamsEqualOld = data.getBooleanExtra(
                    InputControlsActivity.RESULT_SAME_PARAMS, false);
            if (!isNewParamsEqualOld) {
                requestApplyParams();
            }
        }
    }

    @Touch(R.id.btnScrollUp)
    protected boolean scrollUpAction(MotionEvent event) {
        scrollTo(event, new TimerTask() {
            @Override
            public void run() {
                mResourcePresentationService.scrollUp();
            }
        });
        return false;
    }

    @Touch(R.id.btnScrollDown)
    protected boolean scrollDownAction(MotionEvent event) {
        scrollTo(event, new TimerTask() {
            @Override
            public void run() {
                mResourcePresentationService.scrollDown();
            }
        });
        return false;
    }

    //---------------------------------------------------------------------
    // Callbacks
    //---------------------------------------------------------------------

    @Override
    public void onPagePickerRequested() {
        if (paginationBar.isTotalPagesLoaded()) {
            NumberDialogFragment.createBuilder(getSupportFragmentManager())
                    .setMinValue(1)
                    .setCurrentValue(paginationBar.getCurrentPage())
                    .setMaxValue(paginationBar.getTotalPages())
                    .show();
        } else {
            PageDialogFragment.createBuilder(getSupportFragmentManager())
                    .setMaxValue(Integer.MAX_VALUE)
                    .show();
        }
    }

    @Override
    public void onPageSelected(int page, int requestCode) {
        paginationBar.updateCurrentPage(page);
        onPageSelected(page);
    }

    @Override
    public void onPageSelected(int currentPage) {
        paginationBar.setEnabled(false);
        mResourcePresentationService.selectPage(currentPage);
    }

    @Override
    public void showEmptyView() {
        reportMessage.setVisibility(View.VISIBLE);
        reportMessage.setText(getString(R.string.rv_error_empty_report));

        hideScrollControls();
    }

    @Override
    public void hideEmptyView() {
        reportMessage.setVisibility(View.GONE);
    }

    @Override
    public void showErrorView(CharSequence error) {
        reportMessage.setVisibility(View.VISIBLE);
        reportMessage.setText(error);

        hideScrollControls();
    }

    @Override
    public void hideErrorView() {
        reportMessage.setVisibility(View.GONE);
    }

    public void showProgress(CharSequence message) {
        reportProgress.setVisibility(View.VISIBLE);

        reportMessage.setVisibility(View.VISIBLE);
        reportMessage.setText(message);

        hideScrollControls();
    }

    public void hideProgress() {
        reportProgress.setVisibility(View.GONE);
        reportMessage.setVisibility(View.GONE);
    }

    public void showScrollControls() {
        scrollContainer.setVisibility(View.VISIBLE);
    }

    public void hideScrollControls() {
        scrollContainer.setVisibility(View.GONE);
    }

    @Override
    public void onCastStarted() {
        super.onCastStarted();

        showProgress(getString(R.string.r_pd_initializing_msg));
    }

    @Override
    public void onInitializationDone() {
        hideProgress();
        loadInputControls();
    }

    private void loadInputControls() {
        if (controlsLoaded()) {
            requestReportCasting();
        } else {
            loadControls();
        }
    }

    private void loadControls() {
        mGetReportShowControlsPropertyCase.execute(resource.getUri(), new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
                hideProgress();
            }

            @Override
            public void onStart() {
                showProgress(getString(R.string.loading_msg));
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "GetReportShowControlsPropertyCase failed");
                String error = RequestExceptionHandler.extractMessage(ReportCastActivity.this, e);
                showErrorView(error);
            }

            @Override
            public void onNext(Boolean hasControls) {
                mHasControls = hasControls;
                if (hasControls) {
                    InputControlsActivity_.intent(ReportCastActivity.this)
                            .reportUri(resource.getUri())
                            .startForResult(REQUEST_INITIAL_REPORT_PARAMETERS);
                } else {
                    requestReportCasting();
                }
            }
        });
    }

    @Override
    public void onLoadingStarted() {
        invalidateOptionsMenu();
        showProgress(getString(R.string.r_pd_running_report_msg));
        paginationBar.setVisibility(View.GONE);
        paginationBar.reset();
    }

    @Override
    public void onPresentationBegun() {
        invalidateOptionsMenu();
        hideProgress();

        showScrollControls();
    }

    @Override
    public void onMultiPage() {
        paginationBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageCountObtain(int pageCount) {
        if (pageCount == 0) {
            showEmptyView();
        }
        paginationBar.updateTotalCount(pageCount);
        paginationBar.setVisibility(pageCount > 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPageChanged(int pageNumb, String errorMessage) {
        paginationBar.updateCurrentPage(pageNumb);
        paginationBar.setEnabled(true);
        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorOccurred(String error) {
        invalidateOptionsMenu();
        hideProgress();
        showErrorView(error);

        paginationBar.reset();
        paginationBar.setVisibility(View.GONE);
    }

    @Override
    public void onCastStopped() {
        super.onCastStopped();
        finish();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private boolean controlsLoaded() {
        return mHasControls != null;
    }

    private void requestReportCasting() {
        String params = mReportParamsMapper.legacyParamsToJson(getReportParameters());
        mResourcePresentationService.startPresentation(resource, params);
    }

    private void requestApplyParams() {
        String params = mReportParamsMapper.legacyParamsToJson(getReportParameters());
        mResourcePresentationService.applyParams(params);
    }

    private List<InputControl> getInputControls() {
        List<InputControl> inputControls = paramsStorage.getInputControlHolder(resource.getUri()).getInputControls();
        if (inputControls == null) {
            return Collections.emptyList();
        }
        return inputControls;
    }

    private List<ReportParameter> getReportParameters() {
        List<ReportParameter> reportParams = paramsStorage.getInputControlHolder(resource.getUri()).getReportParams();
        if (reportParams == null) {
            return Collections.emptyList();
        }
        return reportParams;
    }

    private void scrollTo(MotionEvent event, TimerTask task) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            cancelScrolling();
            mTimer.scheduleAtFixedRate(task, 0, 10);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            cancelScrolling();
        }
    }

    private void cancelScrolling() {
        mTimer.cancel();
        mTimer.purge();
        mTimer = new Timer();
    }
}
