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

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.CommonRequestListener;
import com.jaspersoft.android.jaspermobile.network.ExceptionRule;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.util.ReportExecutionUtil;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.RunReportExportOutputRequest;
import com.jaspersoft.android.sdk.client.async.request.RunReportExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ErrorDescriptor;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportDataResponse;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.html_viewer_layout)
@OptionsMenu(R.menu.webview_menu)
public class NodeWebViewFragment extends RoboSpiceFragment {

    public static final String TAG = NodeWebViewFragment.class.getSimpleName();
    private static final String COOKIE_STORE = "cookieStore";

    @ViewById
    FrameLayout webViewPlaceholder;
    @ViewById(R.id.htmlViewer_webView_progressBar)
    ProgressBar progressBar;

    @InstanceState
    @FragmentArg
    double versionCode;
    @InstanceState
    @FragmentArg
    int page;
    @InstanceState
    @FragmentArg
    String requestId;

    @InstanceState
    boolean mResourceLoaded;
    @InstanceState
    boolean mOutputFinal;
    @InstanceState
    String executionId;
    @InstanceState
    String currentHtml;

    @Inject
    protected JsRestClient jsRestClient;

    @Bean
    ReportExecutionUtil reportExecutionUtil;
    @Bean
    JSWebViewClient jsWebViewClient;

    private JSWebView webView;

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initWebView();
    }

    @OptionsItem
    final void refreshAction() {
        fetchReport();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (webView != null) webViewPlaceholder.removeView(webView);
        super.onConfigurationChanged(newConfig);
        initWebView();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (webView != null) {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webViewPlaceholder.removeAllViews();
        webView.destroy();
        webView = null;
    }

    public boolean isResourceLoaded() {
        return mResourceLoaded;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void initWebView() {
        // create new if necessary
        if (webView == null) createWebView();
        // attach to placeholder
        if (webView.getParent() != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
        }
        webViewPlaceholder.addView(webView);

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

    private void createWebView() {
        webView = new JSWebView(getActivity(), null, R.style.htmlViewer_webView);
        CookieManagerFactory.syncCookies(getActivity(), jsRestClient);
        prepareWebView();
        setWebViewClient();
    }

    private void setWebViewClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // fade in
                if (progressBar.getAlpha() == 0) {
                    ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f)
                            .setDuration(500).start();
                }
                // update value
                int maxProgress = progressBar.getMax();
                progressBar.setProgress((maxProgress / 100) * progress);
                // fade out
                if (progress == maxProgress) {
                    mResourceLoaded = true;
                    ObjectAnimator.ofFloat(progressBar, "alpha", 1f, 0f)
                            .setDuration(1000).start();
                }
            }
        });
    }

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

    public void fetchReport() {
        final RunReportExportsRequest request = new RunReportExportsRequest(jsRestClient,
                prepareExportsData(), requestId);

        DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!request.isCancelled()) {
                    getSpiceManager().cancel(request);
                }
            }
        };
        DialogInterface.OnShowListener showListener = new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                getSpiceManager().execute(request, new RunReportExportsRequestListener());
            }
        };

        showDialog(cancelListener, showListener);
    }

    private void showDialog(DialogInterface.OnCancelListener cancelListener, DialogInterface.OnShowListener showListener) {
        if (ProgressDialogFragment.isVisible(getFragmentManager())) {
            ProgressDialogFragment.getInstance(getFragmentManager())
                    .setOnCancelListener(cancelListener);
            // Send request
            showListener.onShow(null);
        } else {
            ProgressDialogFragment.show(getFragmentManager(), cancelListener, showListener);
        }
    }

    private ExportsRequest prepareExportsData() {
        ExportsRequest executionData = new ExportsRequest();
        reportExecutionUtil.setupAttachmentPrefix(executionData, versionCode);
        reportExecutionUtil.setupBaseUrl(executionData);
        executionData.setOutputFormat("html");
        executionData.setPages(String.valueOf(page));
        return executionData;
    }

    private void handleFailure(SpiceException exception) {
        Activity activity = getActivity();
        if (isVisible() && activity != null) {
            if (exception instanceof RequestCancelledException) {
                Toast.makeText(activity, R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
            } else {
                RequestExceptionHandler.handle(exception, activity, false);
            }
            ProgressDialogFragment.dismiss(getFragmentManager());
        }
    }

    public void loadFinalOutput() {
        if (!mOutputFinal) fetchReport();
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class RunReportExportsRequestListener implements RequestListener<ExportExecution> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            handleFailure(exception);
            mResourceLoaded = true;
        }

        @Override
        public void onRequestSuccess(ExportExecution response) {
            executionId = response.getId();
            if (versionCode <= ServerInfo.VERSION_CODES.EMERALD_TWO) {
                executionId = ("html;pages=" + page);
            }

            final RunReportExportOutputRequest request = new RunReportExportOutputRequest(jsRestClient,
                    requestId, executionId);

            DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!request.isCancelled()) {
                        getSpiceManager().cancel(request);
                    }
                }
            };
            DialogInterface.OnShowListener showListener = new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    getSpiceManager().execute(request, new RunReportExportOutputRequestListener());
                }
            };

            showDialog(cancelListener, showListener);
        }
    }

    private class RunReportExportOutputRequestListener
            extends CommonRequestListener<ReportDataResponse> {
        private RunReportExportOutputRequestListener() {
            super();
            removeRule(ExceptionRule.FORBIDDEN);
        }

        @Override
        public void onSemanticFailure(SpiceException spiceException) {
            ProgressDialogFragment.dismiss(getFragmentManager());
            mResourceLoaded = true;
            mOutputFinal = true;

            HttpStatus httpStatus = extractStatusCode(spiceException);
            if (httpStatus == HttpStatus.FORBIDDEN) {
                HttpStatusCodeException exception = (HttpStatusCodeException)
                        spiceException.getCause();
                ErrorDescriptor errorDescriptor = ErrorDescriptor.valueOf(exception);

                boolean outOfRange = errorDescriptor.getErrorCode().equals("export.pages.out.of.range");
                if (outOfRange) {
                    AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getActivity().finish();
                                }
                            })
                            .setPositiveButton(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PaginationManagerFragment paginationManagerFragment =
                                            (PaginationManagerFragment) getFragmentManager()
                                                    .findFragmentByTag(PaginationManagerFragment.TAG);
                                    paginationManagerFragment.paginateTo(1);
                                }
                            })
                            .setTitle(R.string.rv_out_of_range)
                            .setMessage(errorDescriptor.getMessage())
                            .setCancelableOnTouchOutside(false)
                            .setNegativeButtonText(android.R.string.cancel)
                            .setPositiveButtonText(R.string.rv_dialog_reload)
                            .show();
                } else {
                    AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(errorDescriptor.getErrorCode())
                            .setMessage(errorDescriptor.getMessage())
                            .setCancelableOnTouchOutside(false)
                            .show();
                }
            } else {
                handleFailure(spiceException);
            }
        }

        @Override
        public void onSemanticSuccess(ReportDataResponse response) {
            ProgressDialogFragment.dismiss(getFragmentManager());
            loadHtml(response.getData());
            mResourceLoaded = true;
            mOutputFinal = response.isFinal();
        }

        @Override
        public Activity getCurrentActivity() {
            return getActivity();
        }
    }

}
