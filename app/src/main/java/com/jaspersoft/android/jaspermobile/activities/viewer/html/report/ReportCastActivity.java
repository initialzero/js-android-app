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
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboCastActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.GetInputControlsFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.GetInputControlsFragment_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializer;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.cast.ResourcePresentationService;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.REQUEST_REPORT_PARAMETERS;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@OptionsMenu({R.menu.webview_menu, R.menu.report_filter_manager_menu})
@EActivity(R.layout.activity_cast_report)
public class ReportCastActivity extends RoboCastActivity implements ReportView, GetInputControlsFragment.OnInputControlsListener, ResourcePresentationService.ResourcePresentationCallback {

    @Extra
    protected ResourceLookup resource;

    @ViewById(R.id.reportScroll)
    protected ScrollView reportScroll;

    @ViewById(R.id.reportScrollPosition)
    protected View reportScrollPosition;

    @ViewById(R.id.progressLoading)
    protected ProgressBar reportProgress;

    @ViewById(R.id.reportMessage)
    protected TextView reportMessage;

    @OptionsMenuItem(R.id.refreshAction)
    protected MenuItem refreshAction;

    @OptionsMenuItem(R.id.showFilters)
    protected MenuItem showFilters;

    @Inject
    protected ReportParamsStorage paramsStorage;
    @Inject
    protected ReportParamsSerializer paramsSerializer;

    private ResourcePresentationService mResourcePresentationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResourcePresentationService = (ResourcePresentationService) ResourcePresentationService.getInstance();
    }

    @AfterViews
    protected void run() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(resource.getLabel());
        }

        reportScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                mResourcePresentationService.scrollTo(calculateScrollPercent());
            }
        });

        mResourcePresentationService.setResourcePresentationCallback(this);
        switch (mResourcePresentationService.getState()) {
            case ResourcePresentationService.INITIALIZED:
                loadInputControls();
                break;
            case ResourcePresentationService.IDLE:
                showProgress(getString(R.string.r_pd_initializing_msg));
                break;
            case ResourcePresentationService.LOADING:
                showProgress(getString(R.string.r_pd_running_report_msg));
                break;
            case ResourcePresentationService.PRESENTING:
                float scrollScale = mResourcePresentationService.getScrollScale();
                setScrollHeight(scrollScale);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!ResourcePresentationService.isStarted()) {
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        paramsStorage.clearInputControlHolder(resource.getUri());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mResourcePresentationService.stopPresentation();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isPresenting = mResourcePresentationService.getState() == ResourcePresentationService.PRESENTING;

        refreshAction.setVisible(isPresenting);
        showFilters.setVisible(isPresenting && !getInputControls().isEmpty());
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem
    public boolean showFilters() {
        if (isInputControlFragmentAdded()) return false;

        addInputControlFragment();
        return true;
    }

    @OptionsItem
    public void refreshAction() {
        mResourcePresentationService.refresh();
        showProgress(getString(R.string.r_pd_running_report_msg));
    }

    @OnActivityResult(REQUEST_REPORT_PARAMETERS)
    final void loadFlowWithControls(int resultCode, Intent data) {
        boolean isPresenting = mResourcePresentationService.getState() == ResourcePresentationService.PRESENTING;
        if (resultCode == Activity.RESULT_OK) {
            boolean isNewParamsEqualOld = data.getBooleanExtra(InputControlsActivity.RESULT_SAME_PARAMS, false);
            if (isNewParamsEqualOld && isPresenting) {
                return;
            }

            if (isPresenting) {
                requestApplyParams();
            } else {
                requestReportCasting();
            }
        } else if (!isPresenting) {
            super.onBackPressed();
        }
    }

    //---------------------------------------------------------------------
    // Callbacks
    //---------------------------------------------------------------------

    @Override
    public void onLoaded() {
        boolean noControls = getInputControls().isEmpty();

        if (noControls) {
            requestReportCasting();
        } else {
            onShowControls();
        }
    }

    @Override
    public void onShowControls() {
        InputControlsActivity_.intent(this).reportUri(resource.getUri()).startForResult(REQUEST_REPORT_PARAMETERS);
    }

    @Override
    public void showEmptyView() {
        reportMessage.setVisibility(View.VISIBLE);
        reportMessage.setText(getString(R.string.rv_error_empty_report));
    }

    @Override
    public void hideEmptyView() {
        reportMessage.setVisibility(View.GONE);
    }

    @Override
    public void showErrorView(CharSequence error) {
        reportMessage.setVisibility(View.VISIBLE);
        reportMessage.setText(error);
    }

    @Override
    public void hideErrorView() {
        reportMessage.setVisibility(View.GONE);
    }

    public void showProgress(CharSequence message) {
        reportProgress.setVisibility(View.VISIBLE);

        reportMessage.setVisibility(View.VISIBLE);
        reportMessage.setText(message);
    }

    public void hideProgress() {
        reportProgress.setVisibility(View.GONE);
        reportMessage.setVisibility(View.GONE);
    }

    @Override
    public void onInitializationDone() {
        hideProgress();
        loadInputControls();
    }

    @Override
    public void onLoadingStarted() {
        invalidateOptionsMenu();
        setScrollHeight(-1);
    }

    @Override
    public void onPresentationBegun() {
        invalidateOptionsMenu();
        hideProgress();

        float scrollScale = mResourcePresentationService.getScrollScale();
        setScrollHeight(scrollScale);
    }

    @Override
    public void onErrorOccurred(String error) {
        invalidateOptionsMenu();
        hideProgress();
        showErrorView(error);
        setScrollHeight(-1);
    }

    @Override
    public void onCastStopped() {
        super.onCastStopped();
        finish();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private boolean isInputControlFragmentAdded() {
        GetInputControlsFragment fragment = (GetInputControlsFragment)
                getSupportFragmentManager().findFragmentByTag(GetInputControlsFragment.TAG);
        return fragment != null;
    }

    private void addInputControlFragment() {
        GetInputControlsFragment fragment = GetInputControlsFragment_.builder()
                .resourceUri(resource.getUri()).build();
        getSupportFragmentManager().beginTransaction()
                .add(fragment, GetInputControlsFragment.TAG).commit();
    }

    private void loadInputControls() {
        if (!getReportParameters().isEmpty()) {
            requestReportCasting();
        } else {
            addInputControlFragment();
        }
    }

    private float calculateScrollPercent() {
        return reportScroll.getScrollY() / (float) (reportScrollPosition.getHeight() - reportScroll.getHeight());
    }

    private void requestReportCasting() {
        mResourcePresentationService.startPresentation(resource.getUri(), paramsSerializer.toJson(getReportParameters()));
        showProgress(getString(R.string.r_pd_running_report_msg));
    }

    private void requestApplyParams() {
        mResourcePresentationService.applyParams(paramsSerializer.toJson(getReportParameters()));
        showProgress(getString(R.string.r_pd_running_report_msg));
    }

    private List<InputControl> getInputControls() {
        return paramsStorage.getInputControlHolder(resource.getUri()).getInputControls();
    }

    private List<ReportParameter> getReportParameters() {
        return paramsStorage.getInputControlHolder(resource.getUri()).getReportParams();
    }

    private void setScrollHeight(float scale) {
        int height;
        if (scale == -1) {
            height = FrameLayout.LayoutParams.MATCH_PARENT;
            reportScroll.setVisibility(View.GONE);
        } else {
            height = (int) (getResources().getDimension(R.dimen.cast_scroll_size) * scale);
            reportScroll.setVisibility(View.VISIBLE);
        }
        reportScrollPosition.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));
    }
}
