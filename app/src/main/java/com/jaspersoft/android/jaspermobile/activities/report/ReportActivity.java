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

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity_;
import com.jaspersoft.android.jaspermobile.activities.save.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.activities.share.AnnotationActivity_;
import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.ReportControlFlags;
import com.jaspersoft.android.jaspermobile.domain.ScreenCapture;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.resource.SaveScreenCaptureCase;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ReportViewModule;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ToolbarActivity;
import com.jaspersoft.android.jaspermobile.ui.view.activity.schedule.NewScheduleActivity_;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.InputControlHolder;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.print.ReportPrintJob;
import com.jaspersoft.android.jaspermobile.util.print.ResourcePrintJob;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceConverter;
import com.jaspersoft.android.jaspermobile.widget.LoadingView;
import com.jaspersoft.android.jaspermobile.widget.SimplePaginationView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.jaspersoft.android.sdk.widget.report.renderer.RunOptions;
import com.jaspersoft.android.sdk.widget.report.renderer.hyperlink.Hyperlink;
import com.jaspersoft.android.sdk.widget.report.view.ReportFragment;
import com.jaspersoft.android.sdk.widget.report.view.ReportFragmentEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class ReportActivity extends ToolbarActivity implements Toolbar.OnMenuItemClickListener, ReportFragmentEventListener {
    public static final String RESOURCE_LOOKUP_ARG = "resource_lookup";
    private static final int REPORT_FILTERS_CODE = 100;

    private ReportFragment reportFragment;
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
    SaveScreenCaptureCase saveScreenCaptureCase;
    @Inject
    ReportParamsStorage reportParamsStorage;
    @Inject
    ReportParamsMapper paramsMapper;
    @Inject
    ReportPrintJob resourcePrintJob;
    @Inject
    JasperResourceConverter mJasperResourceConverter;
    @Inject
    FavoritesHelper favoritesHelper;

    private ResourceLookup resourceLookup;
    private boolean filtersAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report);
        getProfileComponent().plusReportViewer(new ActivityModule(this), new ReportViewModule()).inject(this);
        ButterKnife.bind(this);

        resourceLookup = getResourceLookup();
        initToolbar(resourceLookup.getLabel());
        reportFragment = (ReportFragment) getSupportFragmentManager().findFragmentById(R.id.reportFragment);

        reportFragment.setReportFragmentEventListener(this);
        reportFragment.setPaginationView(paginationView);

        if (!reportFragment.isInited()) {
            reportFragment.init(jasperRestClient.authorizedClient(), map(), 0.5f);
            loadMetadata(resourceLookup.getUri());
        }

        updateMenuItems();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            getReportShowControlsPropertyCase.unsubscribe();
            saveScreenCaptureCase.unsubscribe();
            reportParamsStorage.clearInputControlHolder(resourceLookup.getUri());
        }
    }

    @Override
    public void onActionsAvailabilityChanged() {
        updateMenuItems();
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
                reportFragment.refresh();
                return true;
            case R.id.filtersAction:
                showFiltersPage();
                return true;
            case R.id.favoriteAction:
                favoritesHelper.switchFavoriteState(resourceLookup, item);
                return true;
            case R.id.saveAction:
                showSaveReportPage();
                return true;
            case R.id.printAction:
                showPrintPage();
                return true;
            case R.id.aboutAction:
                showReportInfo();
                return true;
            case R.id.scheduleAction:
                showSchedulePage();
                return true;
            case R.id.shareAction:
                makeScreenShot();
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
            if (reportFragment.isControlActionsAvailable()) {
                reportFragment.applyParams(params);
            } else {
                RunOptions runOptions = new RunOptions.Builder()
                        .reportUri(resourceLookup.getUri())
                        .parameters(params)
                        .build();
                reportFragment.run(runOptions);
            }
        }
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

    private void loadMetadata(String reportUri) {
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

    private void runReport(String reportUri) {
        reportFragment.run(new RunOptions.Builder()
                .reportUri(reportUri)
                .build());
    }

    @NotNull
    private ServerInfo map() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setEdition("CE");
        serverInfo.setVersion(ServerVersion.valueOf(jasperServer.getVersion()));
        return serverInfo;
    }

    private void initToolbar(String title) {
        reportToolbar.inflateMenu(R.menu.report_menu);
        reportToolbar.setOnMenuItemClickListener(this);

        reportToolbar.setTitle(title);
        favoritesHelper.updateFavoriteIconState(reportToolbar.getMenu().findItem(R.id.favoriteAction), resourceLookup.getUri());
        reportToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateMenuItems() {
        boolean renderActionsVisible = reportFragment.isControlActionsAvailable();
        reportToolbar.getMenu().setGroupVisible(R.id.renderedActions, renderActionsVisible);

        MenuItem filtersAction = reportToolbar.getMenu().findItem(R.id.filtersAction);
        filtersAction.setVisible(renderActionsVisible && filtersAvailable);
    }

    private void showFiltersPage() {
        InputControlsActivity_.intent(this)
                .reportUri(resourceLookup.getUri())
                .startForResult(REPORT_FILTERS_CODE);
    }

    private void showSaveReportPage() {
        if (FileUtils.isExternalStorageWritable()) {
            SaveReportActivity_.intent(this)
                    .resource(resourceLookup)
                    .pageCount(getTotalPageCount())
                    .start();
        } else {
            Toast.makeText(this, R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPrintPage() {
        Bundle args = new Bundle();
        args.putString(ReportPrintJob.REPORT_URI_KEY, resourceLookup.getUri());
        args.putInt(ReportPrintJob.TOTAL_PAGES_KEY, getTotalPageCount());
        args.putString(ResourcePrintJob.PRINT_NAME_KEY, resourceLookup.getLabel());

        resourcePrintJob.printResource(args);
    }

    private void showReportInfo() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(resourceLookup.getLabel())
                .setMessage(resourceLookup.getDescription())
                .setNegativeButtonText(R.string.ok)
                .show();
    }

    private void showSchedulePage() {
        JasperResource reportResource = mJasperResourceConverter.convertToJasperResource(resourceLookup);

        NewScheduleActivity_.intent(this)
                .jasperResource(reportResource)
                .start();
    }

    private void showSharePage(File file) {
        Intent intent = AnnotationActivity_.intent(this)
                .imageUri(Uri.fromFile(file))
                .get();
        startActivity(intent);
    }

    private void makeScreenShot() {
        ScreenCapture reportScreenCapture = ScreenCapture.Factory.capture(reportFragment.getView());
        saveScreenCaptureCase.execute(reportScreenCapture, new SimpleSubscriber<File>() {
            @Override
            public void onStart() {
                ProgressDialogFragment.builder(getSupportFragmentManager())
                        .setLoadingMessage(R.string.loading_msg)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                saveScreenCaptureCase.unsubscribe();
                            }
                        })
                        .show();
            }

            @Override
            public void onError(Throwable e) {
                // TODO: Handle screen capture error
            }

            @Override
            public void onNext(File item) {
                ProgressDialogFragment.dismiss(getSupportFragmentManager());
                showSharePage(item);
            }
        });
    }

    private int getTotalPageCount() {
        return paginationView.getTotalPages() == null ? 1 : paginationView.getTotalPages();
    }

}
