/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util.cast;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.util.ScreenUtil_;
import com.jaspersoft.android.jaspermobile.util.VisualizeEndpoint;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
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
import com.jaspersoft.android.jaspermobile.widget.ScrollComputableWebView;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import rx.functions.Action1;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ResourcePresentationService extends CastRemoteDisplayLocalService {

    public final static int IDLE = 0;
    public final static int INITIALIZED = 1;
    public final static int LOADING = 2;
    public final static int PRESENTING = 3;

    private ReportPresentation mPresentation;
    private ResourcePresentationCallback mReportPresentationListener;
    private String mCurrentResource;
    private int mState;

    @Override
    public void onCreatePresentation(Display display) {
        onDismissPresentation();
        mPresentation = new ReportPresentation(this, display);

        try {
            mPresentation.show();
        } catch (WindowManager.InvalidDisplayException ex) {
            onDismissPresentation();
        }
    }

    @Override
    public void onDismissPresentation() {
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
        mState = IDLE;
    }

    public static boolean isStarted() {
        return getInstance() != null;
    }

    public boolean isPresenting() {
        return mState == PRESENTING;
    }

    public void synchronizeState(String resourceUri) {
        if (mCurrentResource != null && !mCurrentResource.equals(resourceUri)) {
            stopPresentation();
        }

        switch (mState) {
            case ResourcePresentationService.IDLE:
                if (mReportPresentationListener != null) {
                    mReportPresentationListener.onCastStarted();
                }
                break;
            case ResourcePresentationService.INITIALIZED:
                if (mReportPresentationListener != null) {
                    mReportPresentationListener.onInitializationDone();
                }
                break;
            case ResourcePresentationService.LOADING:
                if (mReportPresentationListener != null) {
                    mReportPresentationListener.onLoadingStarted();
                }
                break;
            case ResourcePresentationService.PRESENTING:
                if (mReportPresentationListener != null) {
                    mReportPresentationListener.onPresentationBegun();
                }
                break;
        }
    }

    public void setResourcePresentationCallback(ResourcePresentationCallback resourcePresentationCallback) {
        this.mReportPresentationListener = resourcePresentationCallback;
    }

    public void startPresentation(String reportUri, String params) {
        mCurrentResource = reportUri;
        mPresentation.castReport(reportUri, params);
    }

    public void applyParams(String params) {
        stopPresentation();
        mPresentation.applyParams(params);
    }

    public void refresh() {
        stopPresentation();
        mPresentation.refresh();
    }

    public void stopPresentation() {
        mPresentation.hideLoading();
        mPresentation.hideReport();
        mState = INITIALIZED;
    }

    public float getScrollScale() {
        return mPresentation.getContentScale();
    }

    public float getScrollPosition() {
        return mPresentation.calculateScrollPercent();
    }

    public void scrollTo(float scrollPercent) {
        mPresentation.scrollTo(scrollPercent);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void handleInitializationStart() {
        mPresentation.showLoading();
    }

    private void handleInitializationDone() {
        mPresentation.hideLoading();

        mState = INITIALIZED;
        if (mReportPresentationListener != null) {
            mReportPresentationListener.onInitializationDone();
        }
    }

    private void handleLoadStart() {
        mPresentation.showLoading();

        mState = LOADING;
        if (mReportPresentationListener != null) {
            mReportPresentationListener.onLoadingStarted();
        }
    }

    private void handleLoadDone() {
        if (mState == INITIALIZED) return;

        mPresentation.hideLoading();
        mPresentation.showReport();

        mState = PRESENTING;
        if (mReportPresentationListener != null) {
            mReportPresentationListener.onPresentationBegun();
        }
    }

    private void handleError(String error) {
        stopPresentation();
        if (mReportPresentationListener != null) {
            mReportPresentationListener.onErrorOccurred(error);
        }
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    public interface ResourcePresentationCallback {
        void onCastStarted();

        void onInitializationDone();

        void onLoadingStarted();

        void onPresentationBegun();

        void onErrorOccurred(String errorMessage);
    }

    private class ReportPresentation extends CastPresentation implements ErrorWebViewClientListener.OnWebViewErrorListener, ReportCallback {

        private ScrollComputableWebView webView;
        private ProgressBar progressState;

        private AccountServerData accountServerData;

        public ReportPresentation(Context serviceContext, Display display) {
            super(serviceContext, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.report_presentation);

            webView = (ScrollComputableWebView) findViewById(R.id.reportCastWebView);
            progressState = (ProgressBar) findViewById(R.id.progressLoading);

            Account account = JasperAccountManager.get(getContext()).getActiveAccount();
            accountServerData = AccountServerData.get(getContext(), account);

            prepareReportCasting();
        }

        @Override
        public void onWebViewError(String title, String message) {
            handleError(title + "\n" + message);
        }

        //---------------------------------------------------------------------
        // Report presentation commands
        //---------------------------------------------------------------------

        private void castReport(String reportUri, String params) {
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
            final String executeScript = String.format(builder.toString(),
                    accountServerData.getUsername(),
                    accountServerData.getPassword(),
                    organization,
                    ScreenUtil_.getInstance_(getContext()).getDiagonal(),
                    reportUri,
                    params
            );

            webView.loadUrl(executeScript);
        }

        private void applyParams(String params) {
            webView.loadUrl(String.format("javascript:MobileReport.applyReportParams(%s)", params));
        }

        private void refresh() {
            webView.loadUrl("javascript:MobileReport.refresh()");
        }

        private void scrollTo(float scrollPercent) {
            webView.setScrollY((int) ((webView.computeVerticalScrollRange() - webView.getHeight()) * scrollPercent));
        }

        private void showReport() {
            webView.setVisibility(View.VISIBLE);
        }

        private void hideReport() {
            webView.setVisibility(View.INVISIBLE);
        }

        private void showLoading() {
            progressState.setVisibility(View.VISIBLE);
        }

        private void hideLoading() {
            progressState.setVisibility(View.GONE);
        }

        private float getContentScale() {
            return webView.computeVerticalScrollRange() / (float) webView.getHeight();
        }

        private float calculateScrollPercent() {
            int maxScroll = webView.computeVerticalScrollRange() - webView.getHeight();
            if (maxScroll > 0) return webView.getScrollY() / (float) maxScroll;
            return 0;
        }

        //---------------------------------------------------------------------
        // Helper methods
        //---------------------------------------------------------------------

        private void prepareReportCasting() {
            handleInitializationStart();
            CookieManagerFactory.syncCookies(getContext()).subscribe(
                    new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            initWebView();
                            loadVisualize();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            handleError(throwable.getMessage());
                        }
                    });
        }

        private void initWebView() {
            JasperChromeClientListenerImpl chromeClientListener = new JasperChromeClientListenerImpl(new ProgressBar(getContext()));

            DefaultUrlPolicy.SessionListener sessionListener = DefaultSessionListener.from(null);
            UrlPolicy defaultPolicy = DefaultUrlPolicy.from(getContext()).withSessionListener(sessionListener);

            SystemChromeClient systemChromeClient = SystemChromeClient.from(getContext())
                    .withDelegateListener(chromeClientListener);

            JasperWebViewClientListener errorListener = new ErrorWebViewClientListener(getContext(), this);
            JasperWebViewClientListener clientListener = TimeoutWebViewClientListener.wrap(errorListener);

            SystemWebViewClient systemWebViewClient = SystemWebViewClient.newInstance()
                    .withInterceptor(new InjectionRequestInterceptor())
                    .withDelegateListener(clientListener)
                    .withUrlPolicy(defaultPolicy);

            WebInterface mWebInterface = ReportWebInterface.from(this);
            WebViewEnvironment.configure(webView)
                    .withDefaultSettings()
                    .withChromeClient(systemChromeClient)
                    .withWebClient(systemWebViewClient)
                    .withWebInterface(mWebInterface);
        }

        private void loadVisualize() {
            ServerRelease release = ServerRelease.parseVersion(accountServerData.getVersionName());
            // For JRS 6.0 and 6.0.1 we are fixing regression by removing optimization flag
            boolean optimized = !(release.code() >= ServerRelease.AMBER.code() && release.code() <= ServerRelease.AMBER_MR1.code());

            InputStream stream = null;
            try {
                stream = getContext().getAssets().open("report.html");
                StringWriter writer = new StringWriter();
                IOUtils.copy(stream, writer, "UTF-8");

                String baseUrl = accountServerData.getServerUrl();
                VisualizeEndpoint visualizeEndpoint = VisualizeEndpoint.forBaseUrl(baseUrl)
                        .setOptimized(optimized)
                        .build();
                String visualizeUrl = visualizeEndpoint.createUri();

                double initialScale = ScreenUtil_.getInstance_(getContext()).getDiagonal() / 10.1;

                Map<String, Object> data = new HashMap<String, Object>();
                data.put("visualize_url", visualizeUrl);
                data.put("initial_scale", initialScale);
                data.put("optimized", optimized);
                Template tmpl = Mustache.compiler().compile(writer.toString());
                String html = tmpl.execute(data);

                webView.loadDataWithBaseURL(accountServerData.getServerUrl(), html, "text/html", "utf-8", null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (stream != null) {
                    IOUtils.closeQuietly(stream);
                }
            }
        }

        //---------------------------------------------------------------------
        // JS Report callback
        //---------------------------------------------------------------------

        @Override
        public void onScriptLoaded() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    handleInitializationDone();
                }
            });
        }

        @Override
        public void onLoadStart() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    handleLoadStart();
                }
            });
        }

        @Override
        public void onLoadDone(String parameters) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    handleLoadDone();
                }
            });
        }

        @Override
        public void onLoadError(final String error) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    handleError(error);
                }
            });
        }

        @Override
        public void onReportCompleted(String status, int pages, String errorMessage) {
        }

        @Override
        public void onPageChange(int page) {

        }

        @Override
        public void onReferenceClick(String location) {

        }

        @Override
        public void onReportExecutionClick(String data) {

        }

        @Override
        public void onMultiPageStateObtained(boolean isMultiPage) {

        }

        @Override
        public void onWindowError(String errorMessage) {

        }

        @Override
        public void onPageLoadError(String errorMessage, int page) {

        }
    }
}
