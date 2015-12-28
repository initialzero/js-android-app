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
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.cast.CastPresentation;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.GetInputControlsFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.GetInputControlsFragment_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializer;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializerImpl;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
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
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.androidannotations.annotations.UiThread;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Action1;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ReportPresentation extends CastPresentation implements ErrorWebViewClientListener.OnWebViewErrorListener, ReportCallback {

    private WebView webView;
    private ProgressBar loadingBar;
    private TextView message;
    private AccountServerData accountServerData;

    private WebInterface mWebInterface;
    private ResourceLookup resourceLookup;
    private boolean isInited;

    protected ReportParamsStorage paramsStorage;
    protected ReportParamsSerializer paramsSerializer;

    public ReportPresentation(Context serviceContext, Display display) {
        super(serviceContext, display);

        paramsStorage = new ReportParamsStorage();
        paramsSerializer = new ReportParamsSerializerImpl();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.report_presentation);

        webView = (WebView) findViewById(R.id.reportCastWebView);
        loadingBar = (ProgressBar) findViewById(R.id.progressBar);
        message = (TextView) findViewById(R.id.reportMessage);

        Account account = JasperAccountManager.get(getContext()).getActiveAccount();
        accountServerData = AccountServerData.get(getContext(), account);
    }

    public void runReport(ResourceLookup resourceLookup) {
        this.resourceLookup = resourceLookup;

        if (isInited) {
            runReport(paramsSerializer.toJson(getReportParameters()));
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        CookieManagerFactory.syncCookies(getContext()).subscribe(
                new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        hideErrorView();
                        initWebView();
                        loadFlow();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showErrorView(throwable.getMessage());
                    }
                });

    }

    public void stopRunning() {
        webView.setVisibility(View.GONE);
        loadingBar.setVisibility(View.GONE);
        hideErrorView();
    }

    public boolean isReportRunning() {
        return webView.getVisibility() == View.VISIBLE;
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
        final String executeScript = String.format(builder.toString(),
                accountServerData.getUsername(),
                accountServerData.getPassword(),
                organization,
                ScreenUtil_.getInstance_(getContext()).getDiagonal(),
                resourceLookup.getUri(),
                params
        );

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(executeScript);
            }
        });
    }

    private void showErrorView(final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                message.setVisibility(View.VISIBLE);
                message.setText(text);
            }
        });
    }

    private void hideErrorView() {
        message.setVisibility(View.GONE);
    }

    private void showReport() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                loadingBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                loadingBar.setVisibility(View.VISIBLE);
                webView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private List<ReportParameter> getReportParameters() {
        return paramsStorage.getInputControlHolder(resourceLookup.getUri()).getReportParams();
    }

    @Override
    public void onWebViewError(String title, String message) {
        showErrorView(title + "\n" + message);
    }

    @Override
    public void onScriptLoaded() {
        isInited = true;
        runReport(paramsSerializer.toJson(getReportParameters()));
    }

    @Override
    public void onLoadStart() {
        showLoading();
    }

    @Override
    public void onLoadDone(String parameters) {
        showReport();
    }

    @Override
    public void onLoadError(String error) {
        showErrorView(error);
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
