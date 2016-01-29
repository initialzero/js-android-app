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
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportCastActivity_;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
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
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import rx.Subscriber;
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

    @Inject
    private ReportParamsStorage paramsStorage;

    @Inject
    protected Analytics analytics;

    private ReportPresentation mPresentation;
    private String mCastDeviceName;
    private ArrayList<ResourcePresentationCallback> mReportPresentationListeners;
    private ResourceLookup mCurrentResource;
    private int mState;

    @Override
    public void onCreate() {
        super.onCreate();
        RoboGuice.getInjector(getApplicationContext()).injectMembersWithoutViews(this);
        mReportPresentationListeners = new ArrayList<>();
    }

    @Override
    public void onCreatePresentation(Display display) {
        onDismissPresentation();
        mPresentation = new ReportPresentation(this, display);

        try {
            mPresentation.show();
        } catch (WindowManager.InvalidDisplayException ex) {
            onDismissPresentation();
        }
        analytics.sendEvent(Analytics.EventCategory.CAST.getValue(), Analytics.EventAction.PRESENTED.getValue(), null);
    }

    @Override
    public void onDismissPresentation() {
        if (mPresentation != null) {
            clearReportParams();
            resetPresentation();
            mPresentation.dismiss();
            mPresentation = null;
        }
        changeState(IDLE);
    }

    public void setCastDeviceName(String castDeviceName) {
        this.mCastDeviceName = castDeviceName;
    }

    public static boolean isStarted() {
        return getInstance() != null;
    }

    public boolean isPresenting() {
        return mState == PRESENTING;
    }

    private void updateCastNotification() {
        NotificationSettings notificationSettings = new NotificationSettings.Builder()
                .setNotification(createCastNotification())
                .build();
        updateNotificationSettings(notificationSettings);
    }

    public void synchronizeState(ResourceLookup resourceLookup, ResourcePresentationCallback resourcePresentationCallback) {
        if (mCurrentResource != null && !mCurrentResource.getUri().equals(resourceLookup.getUri())) {
            clearReportParams();
            resetPresentation();
        }
        fetchState(resourcePresentationCallback);
    }

    public void fetchState(ResourcePresentationCallback resourcePresentationCallback) {
        switch (mState) {
            case ResourcePresentationService.IDLE:
                if (resourcePresentationCallback != null) {
                    resourcePresentationCallback.onCastStarted();
                }
                break;
            case ResourcePresentationService.INITIALIZED:
                if (resourcePresentationCallback != null) {
                    resourcePresentationCallback.onInitializationDone();
                }
                break;
            case ResourcePresentationService.LOADING:
                if (resourcePresentationCallback != null) {
                    resourcePresentationCallback.onLoadingStarted();
                }
                break;
            case ResourcePresentationService.PRESENTING:
                if (resourcePresentationCallback != null) {
                    resourcePresentationCallback.onPresentationBegun();
                    if (mPresentation.getPageCount() == -1) {
                        resourcePresentationCallback.onMultiPage();
                    } else {
                        resourcePresentationCallback.onPageCountObtain(mPresentation.getPageCount());
                    }
                    resourcePresentationCallback.onPageChanged(mPresentation.getCurrentPage(), null);
                }
                break;
        }
    }

    public synchronized void addResourcePresentationCallback(ResourcePresentationCallback resourcePresentationCallback) {
        this.mReportPresentationListeners.add(resourcePresentationCallback);
    }

    public synchronized void removeResourcePresentationCallback(ResourcePresentationCallback resourcePresentationCallback) {
        this.mReportPresentationListeners.remove(resourcePresentationCallback);
    }

    public void startPresentation(ResourceLookup resourceLookup, String params) {
        mCurrentResource = resourceLookup;
        mPresentation.castReport(resourceLookup.getUri(), params);

        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.PRESENTED.getValue(), resourceLookup.getResourceType().name());
    }

    public void closeCurrentPresentation() {
        clearReportParams();
        resetPresentation();
        for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
            reportPresentationListener.onCastStopped();
        }
        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.PRESENTATION_STOPPED.getValue(), null);
    }

    public void applyParams(String params) {
        resetPresentation();
        mPresentation.applyParams(params);
    }

    public void refresh() {
        resetPresentation();
        mPresentation.refresh();
    }

    public String getCurrentResourceLabel() {
        if (mCurrentResource == null) return null;
        return mCurrentResource.getLabel();
    }

    public Bitmap getCurrentResourceThumbnail() {
        if (mCurrentResource == null || mState != PRESENTING) return null;
        return mPresentation.getThumbnail();
    }

    public void selectPage(int pageNumber) {
        mPresentation.selectPage(pageNumber);
    }

    public void scrollUp() {
        mPresentation.scrollTo(-8);
    }

    public void scrollDown() {
        mPresentation.scrollTo(8);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void changeState(int state) {
        mState = state;
        updateCastNotification();
    }

    private void clearReportParams() {
        if (mCurrentResource != null) {
            paramsStorage.clearInputControlHolder(mCurrentResource.getUri());
        }
    }

    private void resetPresentation() {
        mPresentation.hideLoading();
        mPresentation.hideReport();
        changeState(INITIALIZED);
    }

    private void handleError(String error) {
        resetPresentation();
        for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
            reportPresentationListener.onErrorOccurred(error);
        }
    }

    private Notification createCastNotification() {
        NotificationCompat.Builder castNotificationBuilder = new NotificationCompat.Builder(this);

        Intent intent = NavigationActivity_.intent(this).get();

        String title;

        switch (mState) {
            case IDLE:
                title = getString(R.string.r_pd_initializing_msg);
                break;
            case INITIALIZED:
                title = getString(R.string.cast_ready_message);
                break;
            case LOADING:
                title = getString(R.string.r_pd_running_report_msg);
                intent = ReportCastActivity_.intent(this).resource(mCurrentResource).get();
                break;
            case PRESENTING:
                title = getCurrentResourceLabel();
                intent = ReportCastActivity_.intent(this).resource(mCurrentResource).get();

                castNotificationBuilder
                        .addAction(R.drawable.ic_menu_stop, "", PendingIntent.getBroadcast(this, 0, new Intent(getString(R.string.resource_cast_cancel_intent)), 0))
                        .setLargeIcon(getCurrentResourceThumbnail())
                        .setStyle(new NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0));
                break;
            default:
                title = getString(R.string.r_pd_initializing_msg);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        castNotificationBuilder.setSmallIcon(R.drawable.im_logo_single)
                .setWhen(0)
                .setContentTitle(title)
                .setContentText(mCastDeviceName)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));

        castNotificationBuilder.addAction(R.drawable.ic_menu_close, "", PendingIntent.getBroadcast(this, 0, new Intent(getString(R.string.resource_presentation_stop_intent)), 0));

        return castNotificationBuilder.build();
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    public interface ResourcePresentationCallback {
        void onCastStarted();

        void onInitializationDone();

        void onLoadingStarted();

        void onPresentationBegun();

        void onMultiPage();

        void onPageCountObtain(int pageCount);

        void onPageChanged(int pageNumb, String errorMessage);

        void onErrorOccurred(String errorMessage);

        void onCastStopped();
    }

    private class ReportPresentation extends CastPresentation implements ErrorWebViewClientListener.OnWebViewErrorListener, ReportCallback {

        private ScrollComputableWebView webView;
        private ProgressBar progressState;

        private AccountServerData accountServerData;
        private int mPageCount;
        private int mCurrentPage;

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
        public void onWebViewError(String title, String message, String failingUrl, int errorCode) {
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

        public Bitmap getThumbnail() {
            Bitmap thumbnail = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(thumbnail);
            webView.draw(canvas);
            return thumbnail;
        }

        public void selectPage(int pageNumber) {
            resetZoom();
            webView.loadUrl(String.format("javascript:MobileReport.selectPage(%d)", pageNumber));
        }

        private void scrollTo(int scrollValue) {
            if (webView.canScrollVertically(scrollValue)) {
                webView.scrollBy(0, scrollValue);
            }
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

        private int getPageCount() {
            return mPageCount;
        }

        private int getCurrentPage() {
            return mCurrentPage;
        }

        //---------------------------------------------------------------------
        // Helper methods
        //---------------------------------------------------------------------

        private void prepareReportCasting() {
            mPresentation.showLoading();

            CookieManagerFactory.syncCookies(getContext()).subscribe(new Subscriber<Void>() {
                @Override
                public void onCompleted() {
                    initWebView();
                    loadVisualize();
                }

                @Override
                public void onError(Throwable e) {
                    handleError(e.getMessage());
                }

                @Override
                public void onNext(Void aVoid) {

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
                stream = getContext().getAssets().open("report_cast.html");
                StringWriter writer = new StringWriter();
                IOUtils.copy(stream, writer, "UTF-8");

                String baseUrl = accountServerData.getServerUrl();
                VisualizeEndpoint visualizeEndpoint = VisualizeEndpoint.forBaseUrl(baseUrl)
                        .setOptimized(optimized)
                        .build();
                String visualizeUrl = visualizeEndpoint.createUri();

                double width = ScreenUtil_.getInstance_(getContext()).getWidth() * 1.5;

                Map<String, Object> data = new HashMap<String, Object>();
                data.put("visualize_url", visualizeUrl);
                data.put("width", width);
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

        protected void resetZoom() {
            while (webView.zoomOut()) ;
        }

        //---------------------------------------------------------------------
        // JS Report callback
        //---------------------------------------------------------------------

        @Override
        public void onScriptLoaded() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    hideLoading();

                    changeState(INITIALIZED);
                    for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
                        reportPresentationListener.onInitializationDone();
                    }
                }
            });
        }

        @Override
        public void onLoadStart() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mPresentation.showLoading();

                    changeState(LOADING);
                    mCurrentPage = 1;
                    mPageCount = -1;

                    for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
                        reportPresentationListener.onLoadingStarted();
                    }
                }
            });
        }

        @Override
        public void onLoadDone(String parameters) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mState == INITIALIZED) return;

                    mPresentation.hideLoading();
                    mPresentation.showReport();

                    changeState(PRESENTING);

                    for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
                        reportPresentationListener.onPresentationBegun();
                    }
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
        public void onReportCompleted(String status, final int pages, String errorMessage) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mPageCount = pages;
                    if (pages == 0) {
                        webView.setVisibility(View.GONE);
                    }

                    for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
                        reportPresentationListener.onPageCountObtain(pages);
                    }
                }
            });
        }

        @Override
        public void onPageChange(final int page) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
                        reportPresentationListener.onPageChanged(page, null);
                    }
                    mCurrentPage = page;
                }
            });
        }

        @Override
        public void onReferenceClick(String location) {

        }

        @Override
        public void onReportExecutionClick(String data) {

        }

        @Override
        public void onMultiPageStateObtained(final boolean isMultiPage) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (!isMultiPage) return;

                    for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
                        reportPresentationListener.onMultiPage();
                    }
                }
            });
        }

        @Override
        public void onWindowError(String errorMessage) {

        }

        @Override
        public void onPageLoadError(final String errorMessage, final int page) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (ResourcePresentationCallback reportPresentationListener : mReportPresentationListeners) {
                        reportPresentationListener.onPageChanged(page, errorMessage);
                    }
                    mCurrentPage = page;
                }
            });
        }


        @Override
        public void onAuthError(final String error) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    handleError(error);
                }
            });
        }
    }
}
