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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ExportOutputData;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ReportExportOutputLoader;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ReportSession;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.report_html_viewer)
@OptionsMenu(R.menu.webview_menu)
public class NodeWebViewFragment extends RoboSpiceFragment {
    public static final String TAG = NodeWebViewFragment.class.getSimpleName();

    @ViewById
    protected JSWebView webView;
    @ViewById
    protected ProgressBar progressBar;

    @InstanceState
    @FragmentArg
    protected int page;

    @InstanceState
    protected String currentHtml;
    @InstanceState
    protected boolean outputFinal;

    @Inject
    protected JsRestClient jsRestClient;

    @Bean
    protected JSWebViewClient jsWebViewClient;
    @Bean
    protected ReportSession reportSession;

    private OnPageLoadListener onPageLoadListener;

    @OptionsItem
    final void refreshAction() {
        fetchReport();
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    @AfterViews
    final void init() {
        reportSession.registerObserver(sessionObserver);
        initWebView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reportSession.removeObserver(sessionObserver);
        webView.destroy();
        webView = null;
    }

    public void loadFinalOutput() {
        if (!outputFinal) fetchReport();
    }

    public void setOnPageLoadListener(OnPageLoadListener onPageLoadListener) {
        this.onPageLoadListener = onPageLoadListener;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void initWebView() {
        CookieManagerFactory.syncCookies(getActivity());
        prepareWebView();
        setWebViewClient();
        if (TextUtils.isEmpty(currentHtml)) {
            fetchReport();
        } else {
            loadHtml(currentHtml);
        }
    }

    private void loadHtml(String html) {
        Preconditions.checkNotNull(html);
        Preconditions.checkNotNull(webView);
        Preconditions.checkNotNull(jsRestClient);

        if (!html.equals(currentHtml)) {
            currentHtml = html;
        }
        String mime = "text/html";
        String encoding = "utf-8";
        webView.loadDataWithBaseURL(
                jsRestClient.getServerProfile().getServerUrl(),
                currentHtml, mime, encoding, null);
    }

    private void setWebViewClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    private void prepareWebView() {
        // disable hardware acceleration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        // configure additional settings
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.setWebViewClient(jsWebViewClient);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    private void fetchReport() {
        fetchReport(reportSession.getRequestId());
    }

    private void fetchReport(String requestId) {
        progressBar.setVisibility(View.VISIBLE);
        ReportExportOutputLoader.builder()
                .setControlFragment(this)
                .setExecutionMode(RequestExecutor.Mode.SILENT)
                .setJSRestClient(jsRestClient)
                .setRequestId(requestId)
                .setResultListener(new ExportResultListener())
                .create()
                .loadByPage(page);
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private final ReportSession.SessionObserver sessionObserver = new ReportSession.SessionObserver() {
        @Override
        public void onSessionChanged(String requestId) {
            fetchReport(requestId);
        }
    };

    private class ExportResultListener implements ReportExportOutputLoader.ResultListener {
        @Override
        public void onFailure() {
            progressBar.setVisibility(View.GONE);
            if (onPageLoadListener != null) {
                onPageLoadListener.onFailure();
            }
        }

        @Override
        public void onSuccess(ExportOutputData output) {
            outputFinal = output.isFinal();
            loadHtml(output.getData());
            if (onPageLoadListener != null) {
                onPageLoadListener.onSuccess(page);
            }
        }
    }

    public static interface OnPageLoadListener {
        void onFailure();
        void onSuccess(int page);
    }

}