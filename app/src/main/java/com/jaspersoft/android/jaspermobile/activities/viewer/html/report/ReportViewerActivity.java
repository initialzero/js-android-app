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

import android.accounts.Account;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.InputControlsActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.activities.save.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.GetInputControlsFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.GetInputControlsFragment_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializer;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget.AbstractPaginationView;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget.PaginationBarView;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.dialog.LogDialog;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.PageDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.ScreenUtil;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.jaspermobile.util.VisualizeEndpoint;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.print.JasperPrintJobFactory;
import com.jaspersoft.android.jaspermobile.util.print.JasperPrinter;
import com.jaspersoft.android.jaspermobile.util.print.ResourcePrintJob;
import com.jaspersoft.android.jaspermobile.visualize.HyperlinkHelper;
import com.jaspersoft.android.jaspermobile.webview.DefaultSessionListener;
import com.jaspersoft.android.jaspermobile.webview.DefaultUrlPolicy;
import com.jaspersoft.android.jaspermobile.webview.ErrorWebViewClientListener;
import com.jaspersoft.android.jaspermobile.webview.JasperChromeClientListenerImpl;
import com.jaspersoft.android.jaspermobile.webview.JasperWebViewClientListener;
import com.jaspersoft.android.jaspermobile.webview.SystemChromeClient;
import com.jaspersoft.android.jaspermobile.webview.SystemWebViewClient;
import com.jaspersoft.android.jaspermobile.webview.TimeoutWebViewClientListener;
import com.jaspersoft.android.jaspermobile.webview.UrlPolicy;
import com.jaspersoft.android.jaspermobile.webview.WebInterface;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;
import com.jaspersoft.android.jaspermobile.webview.dashboard.InjectionRequestInterceptor;
import com.jaspersoft.android.jaspermobile.webview.report.bridge.ReportCallback;
import com.jaspersoft.android.jaspermobile.webview.report.bridge.ReportWebInterface;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.REQUEST_REPORT_PARAMETERS;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@OptionsMenu({R.menu.retrofit_report_menu, R.menu.webview_menu, R.menu.report_filter_manager_menu})
@EActivity(R.layout.activity_report_viewer)
public class ReportViewerActivity extends RoboToolbarActivity
        implements ReportCallback,
        AbstractPaginationView.OnPageChangeListener,
        GetInputControlsFragment.OnInputControlsListener,
        ReportView, PageDialogFragment.PageDialogClickListener,
        NumberDialogFragment.NumberDialogClickListener,
        ErrorWebViewClientListener.OnWebViewErrorListener
{

    @Bean
    protected JSWebViewClient jsWebViewClient;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;
    @Bean
    protected FavoritesHelper favoritesHelper;
    @Bean
    protected ScreenUtil screenUtil;
    @Bean
    protected HyperlinkHelper hyperlinkHelper;

    @ViewById
    protected WebView webView;
    @ViewById(android.R.id.empty)
    protected TextView emptyView;
    @ViewById
    protected ProgressBar progressBar;
    @ViewById
    protected PaginationBarView paginationControl;

    @Extra
    protected ResourceLookup resource;
    @Extra
    protected ArrayList<ReportParameter> reportParameters = new ArrayList<ReportParameter>();

    @InstanceState
    protected Uri favoriteEntryUri;

    @OptionsMenuItem
    protected MenuItem favoriteAction;
    @OptionsMenuItem
    protected MenuItem saveReport;
    @OptionsMenuItem
    protected MenuItem printAction;
    @OptionsMenuItem
    protected MenuItem refreshAction;

    @Inject
    protected ReportParamsStorage paramsStorage;
    @Inject
    protected ReportParamsSerializer paramsSerializer;
    @Inject
    protected JsRestClient jsRestClient;
    @Inject
    protected Analytics analytics;

    private AccountServerData accountServerData;
    private boolean mShowSaveAndPrintMenuItems, mShowRefreshMenuItem;
    private boolean mHasInitialParameters;
    private JasperChromeClientListenerImpl chromeClientListener;
    private WebInterface mWebInterface;
    private boolean isFlowLoaded;

    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            ReportViewerActivity.super.onBackPressed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHasInitialParameters = !reportParameters.isEmpty();
        paramsStorage.getInputControlHolder(resource.getUri()).setReportParams(reportParameters);

        scrollableTitleHelper.injectTitle(resource.getLabel());

        if (savedInstanceState == null) {
            favoriteEntryUri = favoritesHelper.queryFavoriteUri(resource);
        }

        Account account = JasperAccountManager.get(this).getActiveAccount();
        accountServerData = AccountServerData.get(this, account);
    }

    @AfterViews
    final void init() {
        showErrorView(getString(R.string.loading_msg));

        Subscription cookieSubscription = CookieManagerFactory.syncCookies(this).subscribe(
                new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        hideErrorView();
                        setupPaginationControl();
                        initWebView();
                        loadInputControls();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showErrorView(throwable.getMessage());
                    }
                });
        mCompositeSubscription.add(cookieSubscription);
    }

    private void loadInputControls() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        GetInputControlsFragment fragment = (GetInputControlsFragment)
                fragmentManager.findFragmentByTag(GetInputControlsFragment.TAG);
        if (fragment == null) {
            fragment = GetInputControlsFragment_.builder()
                    .resourceUri(resource.getUri()).build();
            getSupportFragmentManager().beginTransaction()
                    .add(fragment, GetInputControlsFragment.TAG).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        favoriteAction.setIcon(favoriteEntryUri == null ? R.drawable.ic_menu_star_outline : R.drawable.ic_menu_star);
        favoriteAction.setTitle(favoriteEntryUri == null ? R.string.r_cm_add_to_favorites : R.string.r_cm_remove_from_favorites);
        saveReport.setVisible(mShowSaveAndPrintMenuItems);
        refreshAction.setVisible(mShowRefreshMenuItem);
        if (printAction != null) {
            printAction.setVisible(mShowSaveAndPrintMenuItems);
        }

        if (BuildConfig.FLAVOR.equals("qa") || BuildConfig.FLAVOR.equals("dev")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.debug, menu);
        }

        return result;
    }

    @Override
    protected void onPause() {
        if (mWebInterface != null) {
            mWebInterface.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebInterface != null) {
            mWebInterface.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (webView != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.removeAllViews();
            webView.destroy();
        }
        paramsStorage.clearInputControlHolder(resource.getUri());
    }

    //---------------------------------------------------------------------
    // Menu items callbacks
    //---------------------------------------------------------------------

    @OptionsItem
    final void showLog() {
        if (chromeClientListener != null) {
            LogDialog.create(getSupportFragmentManager(), chromeClientListener.getMessages());
        }
    }

    @OptionsItem
    final void favoriteAction() {
        favoriteEntryUri = favoritesHelper.
                handleFavoriteMenuAction(favoriteEntryUri, resource, favoriteAction);
    }

    @OptionsItem
    final void aboutAction() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(resource.getLabel())
                .setMessage(resource.getDescription())
                .setNegativeButtonText(R.string.ok)
                .show();
    }

    @OptionsItem
    public void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            SaveReportActivity_.intent(this)
                    .resource(resource)
                    .pageCount(paginationControl.getTotalPages())
                    .start();
        } else {
            Toast.makeText(this,
                    R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @OptionsItem
    public void refreshAction() {
        resetZoom();
        webView.loadUrl("javascript:MobileReport.refresh()");
        ProgressDialogFragment
                .builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.r_ab_refresh)
                .setOnCancelListener(cancelListener)
                .show();
        webView.setVisibility(View.INVISIBLE);
        paginationControl.setVisibility(View.GONE);
        paginationControl.reset();
    }

    @OptionsItem
    final void printAction() {
        analytics.sendEvent(Analytics.EventCategory.PRINT.getValue(), Analytics.EventAction.CLICK.getValue(), Analytics.EventLabel.REPORT.getValue());
        ResourcePrintJob job = JasperPrintJobFactory
                .createReportPrintJob(this, jsRestClient, resource, reportParameters);
        JasperPrinter.print(job);
    }

    //---------------------------------------------------------------------
    // Input controls loading callbacks
    //---------------------------------------------------------------------

    @Override
    public void onLoaded() {
        boolean noControls = getInputControls().isEmpty();

        if (noControls) {
            loadFlow();
        } else {
            if (mHasInitialParameters) {
                loadFlow();
            } else {
                showInputControlsPage();
            }
        }
    }

    @Override
    public void onShowControls() {
        showInputControlsPage();
    }

    @OnActivityResult(REQUEST_REPORT_PARAMETERS)
    final void loadFlowWithControls(int resultCode, Intent data) {
        // By default we make webview invisible
        // If any report run successful we will have this condition to be falsy
        boolean isFirstReportMissing = (webView.getVisibility() == View.INVISIBLE);

        if (resultCode == Activity.RESULT_OK) {
            boolean isNewParamsEqualOld = data.getBooleanExtra(InputControlsActivity.RESULT_SAME_PARAMS, false);
            if (isNewParamsEqualOld && !isFirstReportMissing) {
                return;
            }

            mShowSaveAndPrintMenuItems = false;
            supportInvalidateOptionsMenu();
            if (isFlowLoaded) {
                applyReportParams();
            } else {
                loadFlow();
            }
        } else if (isFirstReportMissing) {
            super.onBackPressed();
        }
    }

    //---------------------------------------------------------------------
    // Pagination callbacks
    //---------------------------------------------------------------------

    @Override
    public void onPageSelected(int page, int requestCode) {
        paginationControl.updateCurrentPage(page);
        onPageSelected(page);
    }

    @Override
    public void onPageSelected(int currentPage) {
        resetZoom();
        paginationControl.setEnabled(false);
        selectPageInWebView(currentPage);
    }

    @Override
    public void onPagePickerRequested() {
        if (paginationControl.isTotalPagesLoaded()) {
            NumberDialogFragment.createBuilder(getSupportFragmentManager())
                    .setMinValue(1)
                    .setCurrentValue(paginationControl.getCurrentPage())
                    .setMaxValue(paginationControl.getTotalPages())
                    .show();
        } else {
            PageDialogFragment.createBuilder(getSupportFragmentManager())
                    .setMaxValue(Integer.MAX_VALUE)
                    .show();
        }
    }

    //---------------------------------------------------------------------
    // Javascript callbacks
    //---------------------------------------------------------------------

    @UiThread
    @Override
    public void onScriptLoaded() {
        runReport(paramsSerializer.toJson(getReportParameters()));
    }

    @UiThread
    @Override
    public void onLoadStart() {
        paginationControl.reset();
        paginationControl.setVisibility(View.GONE);
        ProgressDialogFragment
                .builder(getSupportFragmentManager())
                .setOnCancelListener(cancelListener).show();
        webView.setVisibility(View.INVISIBLE);
    }

    @UiThread
    @Override
    public void onLoadDone(String parameters) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        webView.setVisibility(View.VISIBLE);
    }

    @UiThread
    @Override
    public void onLoadError(String error) {
        exposeError(error);
    }

    @UiThread
    @Override
    public void onReportCompleted(String status, int pages, String errorMessage) {
        if (status.equals("ready")) {
            boolean noPages = (pages == 0);
            mShowSaveAndPrintMenuItems = mShowRefreshMenuItem = !noPages;
            supportInvalidateOptionsMenu();

            paginationControl.updateTotalCount(pages);
            paginationControl.setVisibility(pages > 1 ? View.VISIBLE : View.GONE);
            if (noPages) showEmptyView();
            else hideEmptyView();
        }
    }

    @UiThread
    @Override
    public void onPageChange(int page) {
        paginationControl.updateCurrentPage(page);
        paginationControl.setEnabled(true);
    }

    @UiThread
    @Override
    public void onPageLoadError(String errorMessage, int page) {
        paginationControl.updateCurrentPage(page);
        paginationControl.setEnabled(true);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    @Override
    public void onReferenceClick(String location) {
        String title = getString(R.string.rv_open_link_chooser);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
        Intent chooser = Intent.createChooser(browserIntent, title);
        if (browserIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    @Override
    public void onReportExecutionClick(String data) {
        hyperlinkHelper.executeReport(data);
    }

    @UiThread
    @Override
    public void onMultiPageStateObtained(boolean isMultiPage) {
        // Because of bug on JRS (return true for empty report) we need to check that count != 0
        boolean needToShowPagination = isMultiPage && paginationControl.getTotalPages() != 0;
        paginationControl.setVisibility(needToShowPagination ? View.VISIBLE : View.GONE);
    }

    @UiThread
    @Override
    public void onWindowError(String errorMessage) {
        showErrorView(getString(R.string.sr_failed_to_execute_report));
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    //---------------------------------------------------------------------
    // ReportView callbacks
    //---------------------------------------------------------------------

    @Override
    public void showEmptyView() {
        emptyView.setText(R.string.rv_error_empty_report);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
    }

    @Override
    public void showErrorView(CharSequence error) {
        if (!TextUtils.isEmpty(error)) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(error);
        }
    }

    @Override
    public void hideErrorView() {
        emptyView.setVisibility(View.GONE);
    }

    //---------------------------------------------------------------------
    // ErrorWebViewClientListener.OnWebViewErrorListener
    //---------------------------------------------------------------------

    @Override
    public void onWebViewError(String title, String message) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        showErrorView(title + "\n" + message);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    protected void resetZoom() {
        while (webView.zoomOut()) ;
    }

    private void setupPaginationControl() {
        paginationControl.setOnPageChangeListener(this);
    }

    private void initWebView() {
        chromeClientListener = new JasperChromeClientListenerImpl(progressBar);

        DefaultUrlPolicy.SessionListener sessionListener = DefaultSessionListener.from(this);
        UrlPolicy defaultPolicy = DefaultUrlPolicy.from(this).withSessionListener(sessionListener);

        SystemChromeClient systemChromeClient = SystemChromeClient.from(this)
                .withDelegateListener(chromeClientListener);

        JasperWebViewClientListener errorListener = new ErrorWebViewClientListener(this, this);
        JasperWebViewClientListener clientListener = TimeoutWebViewClientListener.wrap(errorListener);

        SystemWebViewClient systemWebViewClient = SystemWebViewClient.newInstance()
                .withInterceptor(new InjectionRequestInterceptor())
                .withDelegateListener(clientListener)
                .withUrlPolicy(defaultPolicy);

        mWebInterface = ReportWebInterface.from(this);
        WebViewEnvironment.configure(webView)
                .withDefaultSettings()
                .withChromeClient(systemChromeClient)
                .withWebClient(systemWebViewClient)
                .withWebInterface(mWebInterface);
    }

    private void loadFlow() {
        ServerRelease release = ServerRelease.parseVersion(accountServerData.getVersionName());
        // For JRS 6.0 and 6.0.1 we are fixing regression by removing optimization flag
        boolean optimized = !(release.code() >= ServerRelease.AMBER.code() && release.code() <= ServerRelease.AMBER_MR1.code());

        InputStream stream = null;
        try {
            stream = getAssets().open("report.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");


            String baseUrl = accountServerData.getServerUrl();
            VisualizeEndpoint visualizeEndpoint = VisualizeEndpoint.forBaseUrl(baseUrl)
                    .setOptimized(optimized)
                    .build();
            String visualizeUrl = visualizeEndpoint.createUri();

            double initialScale = screenUtil.getDiagonal() / 10.1;

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("visualize_url", visualizeUrl);
            data.put("initial_scale", initialScale);
            data.put("optimized", optimized);
            Template tmpl = Mustache.compiler().compile(writer.toString());
            String html = tmpl.execute(data);

            isFlowLoaded = true;
            webView.setVisibility(View.VISIBLE);
            webView.loadDataWithBaseURL(accountServerData.getServerUrl(), html, "text/html", "utf-8", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    private void showInputControlsPage() {
        InputControlsActivity_.intent(this).reportUri(resource.getUri()).startForResult(REQUEST_REPORT_PARAMETERS);
    }

    private void runReport(String params) {
        String organization = TextUtils.isEmpty(accountServerData.getOrganization())
                ? "" : accountServerData.getOrganization();

        StringBuilder builder = new StringBuilder();
        builder.append("javascript:MobileReport.configure")
                .append("({ \"auth\": ")
                .append("{")
                .append("\"username\": \"%s\",")
                .append("\"password\": \"%s\",")
                .append("\"organization\": \"%s\"")
                .append("}, ")
                .append("\"diagonal\": %s ")
                .append("})")
                .append(".run({")
                .append("\"uri\": \"%s\",")
                .append("\"params\": %s")
                .append("})");
        String executeScript = String.format(builder.toString(),
                accountServerData.getUsername(),
                accountServerData.getPassword(),
                organization,
                screenUtil.getDiagonal(),
                resource.getUri(),
                params
        );
        webView.loadUrl(executeScript);
    }

    private void applyReportParams() {
        String reportParams = paramsSerializer.toJson(getReportParameters());
        resetZoom();
        webView.loadUrl(String.format("javascript:MobileReport.applyReportParams(%s)", reportParams));
    }

    private List<InputControl> getInputControls() {
        return paramsStorage.getInputControlHolder(resource.getUri()).getInputControls();
    }

    private List<ReportParameter> getReportParameters() {
        return paramsStorage.getInputControlHolder(resource.getUri()).getReportParams();
    }

    private void selectPageInWebView(int page) {
        webView.loadUrl(String.format("javascript:MobileReport.selectPage(%d)", page));
    }

    private void exposeError(String error) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        showErrorView(error);
    }
}
