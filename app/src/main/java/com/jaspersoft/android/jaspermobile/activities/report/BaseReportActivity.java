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

package com.jaspersoft.android.jaspermobile.activities.report;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity_;
import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.ReportControlFlags;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.ui.view.activity.CastActivity;
import com.jaspersoft.android.jaspermobile.util.InputControlHolder;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.widget.LoadingView;
import com.jaspersoft.android.jaspermobile.widget.SimplePaginationView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.widget.report.renderer.RunOptions;
import com.jaspersoft.android.sdk.widget.report.renderer.hyperlink.Hyperlink;
import com.jaspersoft.android.sdk.widget.report.view.ReportEventListener;
import com.jaspersoft.android.sdk.widget.report.view.ReportViewer;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public abstract class BaseReportActivity extends CastActivity implements Toolbar.OnMenuItemClickListener, ReportEventListener {
    public static final String RESOURCE_LOOKUP_ARG = "resource_lookup";
    private static final int REPORT_FILTERS_CODE = 100;

    protected ReportViewer reportViewer;
    @BindView(R.id.loading)
    LoadingView loading;
    @BindView(R.id.reportToolbar)
    Toolbar reportToolbar;
    @BindView(R.id.paginationControl)
    SimplePaginationView paginationView;

    @Inject
    JasperServer jasperServer;
    @Inject
    JasperRestClient jasperRestClient;
    @Inject
    GetReportShowControlsPropertyCase getReportShowControlsPropertyCase;
    @Inject
    ReportParamsStorage reportParamsStorage;
    @Inject
    ReportParamsMapper paramsMapper;

    protected ResourceLookup resourceLookup;
    private boolean filtersAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(provideContentView());
        ButterKnife.bind(this);

        resourceLookup = getResourceLookup();
        initToolbar(resourceLookup.getLabel());
    }

    @Override
    public void onActionsAvailabilityChanged(boolean isAvailable) {
        reportToolbar.getMenu().setGroupEnabled(R.id.renderedActions, isAvailable);

        MenuItem filtersAction = reportToolbar.getMenu().findItem(R.id.filtersAction);
        filtersAction.setVisible(filtersAvailable);
    }

    @Override
    public void onHyperlinkClicked(Hyperlink hyperlink) {

    }

    @Override
    public void onError(ServiceException exception) {
        // TODO: Handle report error
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshAction:
                reportViewer.refresh();
                return true;
            case R.id.filtersAction:
                showFiltersPage();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REPORT_FILTERS_CODE) {
            InputControlHolder icHolder = reportParamsStorage.getInputControlHolder(resourceLookup.getUri());
            List<ReportParameter> params = paramsMapper.legacyParamsToRetrofitted(icHolder.getReportParams());
            if (reportViewer.isControlActionsAvailable()) {
                reportViewer.applyParams(params);
            } else {
                RunOptions runOptions = new RunOptions.Builder()
                        .reportUri(resourceLookup.getUri())
                        .parameters(params)
                        .build();
                reportViewer.run(runOptions);
            }
        }
    }

    protected abstract int provideContentView();

    protected abstract int provideItemsMenu();

    protected abstract float provideScale();

    protected final boolean init(ReportViewer reportViewer) {
        this.reportViewer = reportViewer;

        reportViewer.setReportEventListener(this);
        reportViewer.setPaginationView(paginationView);

        boolean inited = false;
        if (!reportViewer.isInited()) {
            reportViewer.init(jasperRestClient.authorizedClient(), mapServerInfo(), provideScale());
            inited = true;
        }

        return inited;
    }

    protected void loadMetadata(String reportUri) {
        getReportShowControlsPropertyCase.execute(reportUri, new SimpleSubscriber<ReportControlFlags>() {
            @Override
            public void onNext(ReportControlFlags flags) {
                filtersAvailable = flags.hasControls();

                boolean needPrompt = flags.needPrompt();
                if (filtersAvailable && needPrompt) {
                    showFiltersPage();
                } else {
                    runReport(resourceLookup.getUri());
                }
            }
        });
    }

    protected void initToolbar(String title) {
        reportToolbar.inflateMenu(provideItemsMenu());
        reportToolbar.setOnMenuItemClickListener(this);

        reportToolbar.setTitle(title);
        reportToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        onToolbarMenuCreated(reportToolbar.getMenu().findItem(R.id.castAction));
    }

    @NotNull
    private ResourceLookup getResourceLookup() {
        Bundle extras = getIntent().getExtras();
        ResourceLookup resourceLookup = null;
        if (extras != null) {
            resourceLookup = extras.getParcelable(RESOURCE_LOOKUP_ARG);
        }
        if (resourceLookup == null) {
            throw new RuntimeException("Resource lookup should not be provided");
        }
        return resourceLookup;
    }

    private void runReport(String reportUri) {
        reportViewer.run(new RunOptions.Builder()
                .reportUri(reportUri)
                .build());
    }

    @NotNull
    private ServerInfo mapServerInfo() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setEdition(jasperServer.getEdition());
        serverInfo.setVersion(ServerVersion.valueOf(jasperServer.getVersion()));
        return serverInfo;
    }

    private void showFiltersPage() {
        InputControlsActivity_.intent(this)
                .reportUri(resourceLookup.getUri())
                .startForResult(REPORT_FILTERS_CODE);
    }
}
