/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.ReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.DashboardCordovaWebClient;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.GetInputControlsFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.GetInputControlsFragment_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.model.ReportModel;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.CordovaInterfaceImpl;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.SessionListener;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.SimpleChromeClient;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.bridge.ReportCallback;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.bridge.ReportWebInterface;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget.AbstractPaginationView;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget.PaginationBarView;
import com.jaspersoft.android.jaspermobile.dialog.LogDialog;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
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
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.Whitelist;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.EXTRA_REPORT_CONTROLS;
import static com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity.REQUEST_REPORT_PARAMETERS;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@OptionsMenu({R.menu.retrofit_report_menu})
@EActivity(R.layout.activity_cordova_dashboard_viewer)
public class ReportViewerActivity extends RoboToolbarActivity
        implements ReportCallback,
        AbstractPaginationView.OnPageChangeListener,
        GetInputControlsFragment.OnInputControlsListener {

    @Bean
    protected JSWebViewClient jsWebViewClient;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;
    @Bean
    protected FavoritesHelper favoritesHelper;

    @ViewById
    protected CordovaWebView webView;
    @ViewById(android.R.id.empty)
    protected TextView emptyView;
    @ViewById
    protected ProgressBar progressBar;
    @ViewById
    protected PaginationBarView paginationControl;

    @Extra
    protected ResourceLookup resource;
    @Extra
    protected ArrayList<ReportParameter> reportParameters;

    @InstanceState
    protected Uri favoriteEntryUri;
    @InstanceState
    protected ReportModel reportModel = new ReportModel();

    @OptionsMenuItem
    protected MenuItem favoriteAction;
    @OptionsMenuItem
    protected MenuItem saveReport;

    private SimpleChromeClient chromeClient;
    private AccountServerData accountServerData;
    private boolean mShowSavedMenuItem;
    private boolean mHasInitialParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHasInitialParameters = (reportParameters != null);
        if (mHasInitialParameters) {
            reportModel.setReportParameters(reportParameters);
        }

        scrollableTitleHelper.injectTitle(resource.getLabel());

        if (savedInstanceState == null) {
            favoriteEntryUri = favoritesHelper.queryFavoriteUri(resource);
        }

        Account account = JasperAccountManager.get(this).getActiveAccount();
        accountServerData = AccountServerData.get(this, account);
    }

    @AfterViews
    final void init() {
        setupPaginationControl();
        setupSettings();
        setupJsInterface();
        initCordovaWebView();
        loadInputControls();
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
        saveReport.setVisible(mShowSavedMenuItem);

        if (BuildConfig.FLAVOR.equals("qa") || BuildConfig.FLAVOR.equals("dev")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.debug, menu);
        }

        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.webView != null) {
            webView.handleDestroy();
        }
    }

    //---------------------------------------------------------------------
    // Menu items callbacks
    //---------------------------------------------------------------------

    @OptionsItem
    final void showLog() {
        if (chromeClient != null) {
            LogDialog.create(getSupportFragmentManager(), chromeClient.getMessages());
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
                .setNegativeButtonText(android.R.string.ok)
                .show();
    }

    @OptionsItem
    public void saveReport() {
        if (FileUtils.isExternalStorageWritable()) {
            SaveReportActivity_.intent(this)
                    .reportParameters(reportModel.getReportParameters())
                    .resource(resource)
                    .pageCount(paginationControl.getTotalPages())
                    .start();
        } else {
            Toast.makeText(this,
                    R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    //---------------------------------------------------------------------
    // Input controls loading callbacks
    //---------------------------------------------------------------------

    @Override
    public void onLoaded(List<InputControl> inputControls) {
        boolean noControls = inputControls.isEmpty();

        if (noControls) {
            loadFlow();
        } else {
            if (mHasInitialParameters) {
                loadFlow();
            } else {
                showInputControlsPage(inputControls);
            }
        }
    }

    @Override
    public void onShowControls() {
        showInputControlsPage(reportModel.getInputControls());
    }

    @OnActivityResult(REQUEST_REPORT_PARAMETERS)
    final void loadFlowWithControls(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            ArrayList<InputControl> inputControl = data.getParcelableArrayListExtra(EXTRA_REPORT_CONTROLS);
            reportModel.setInputControls(inputControl);
            reportModel.updateReportParameters();

            mShowSavedMenuItem = false;
            supportInvalidateOptionsMenu();
            loadFlow();
        } else {
            // By default we make webview invisible
            // If any report run successful we will have this condition to be falsy
            boolean isFirstReportMissing = (webView.getVisibility() == View.INVISIBLE);
            if (isFirstReportMissing) {
                super.onBackPressed();
            }
        }
    }

    //---------------------------------------------------------------------
    // Pagination callbacks
    //---------------------------------------------------------------------

    @Override
    public void onPageSelected(int currentPage) {
        webView.loadUrl(String.format("javascript:MobileReport.selectPage(%d)", currentPage));
    }

    @Override
    public void onPickerSelected(boolean pickExactPage) {
    }

    //---------------------------------------------------------------------
    // Javascript callbacks
    //---------------------------------------------------------------------

    @UiThread
    @Override
    public void onScriptLoaded() {
        runReport(reportModel.getJsonReportParameters());
    }

    @UiThread
    @Override
    public void onLoadStart() {
        ProgressDialogFragment.builder(getSupportFragmentManager()).show();
    }

    @UiThread
    @Override
    public void onLoadDone(String parameters) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    @UiThread
    @Override
    public void onLoadError(String error) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @UiThread
    @Override
    public void onTotalPagesLoaded(int pages) {
        boolean noPages = (pages == 0);
        mShowSavedMenuItem = !noPages;
        supportInvalidateOptionsMenu();

        if (noPages) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            if (pages > 1) {
                paginationControl.setVisibility(View.VISIBLE);
                paginationControl.setTotalCount(pages);
            }
        }
    }

    @UiThread
    @Override
    public void onPageChange(int page) {
        paginationControl.setCurrentPage(page);
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
    public void onReportExecutionClick(String reportUri, String params) {
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void setupPaginationControl() {
        paginationControl.setOnPageChangeListener(this);
    }

    private void setupSettings() {
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        }
    }

    @SuppressLint({"AddJavascriptInterface"})
    private void setupJsInterface() {
        webView.addJavascriptInterface(new ReportWebInterface(this), "Android");
    }

    private void initCordovaWebView() {
        Whitelist whitelist = new Whitelist();
        whitelist.addWhiteListEntry("http://*/*", true);
        whitelist.addWhiteListEntry("https://*/*", true);
        CordovaPreferences cordovaPreferences = new CordovaPreferences();

        jsWebViewClient.setSessionListener(new SessionListener(this));

        CordovaInterface cordovaInterface = new CordovaInterfaceImpl(this);
        CordovaWebViewClient webViewClient2 = new DashboardCordovaWebClient(cordovaInterface, webView, jsWebViewClient);

        chromeClient = new SimpleChromeClient(cordovaInterface, webView, progressBar);

        List<PluginEntry> pluginEntries = (List<PluginEntry>) Collections.EMPTY_LIST;

        webView.init(cordovaInterface, webViewClient2, chromeClient, pluginEntries, whitelist, whitelist, cordovaPreferences);
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

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("visualize_url", accountServerData.getServerUrl() + "/client/visualize.js?_opt=" + optimized);
            data.put("optimized", optimized);
            Template tmpl = Mustache.compiler().compile(writer.toString());
            String html = tmpl.execute(data);

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

    private void showInputControlsPage(List<InputControl> inputControls) {
        Intent intent = new Intent(this, ReportOptionsActivity.class);
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_URI, resource.getUri());
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_LABEL, resource.getLabel());
        intent.putParcelableArrayListExtra(
                ReportOptionsActivity.EXTRA_REPORT_CONTROLS, new ArrayList<Parcelable>(inputControls));
        startActivityForResult(intent, REQUEST_REPORT_PARAMETERS);
    }

    private void runReport(String params) {
        String organization = TextUtils.isEmpty(accountServerData.getOrganization())
                ? "" : accountServerData.getOrganization();

        StringBuilder builder = new StringBuilder();
        builder.append("javascript:MobileReport.setCredentials")
                .append("({")
                .append("\"username\": \"%s\",")
                .append("\"password\": \"%s\",")
                .append("\"organization\": \"%s\"")
                .append("})");
        String authScript = String.format(builder.toString(),
                accountServerData.getUsername(),
                accountServerData.getPassword(),
                organization);
        webView.loadUrl(authScript);

        builder = new StringBuilder();
        builder.append("javascript:MobileReport.run")
                .append("({")
                .append("\"uri\": \"%s\",")
                .append("\"params\": %s")
                .append("})");

        String executeScript = String.format(builder.toString(),
                resource.getUri(),
                params);
        webView.loadUrl(executeScript);
    }

}
