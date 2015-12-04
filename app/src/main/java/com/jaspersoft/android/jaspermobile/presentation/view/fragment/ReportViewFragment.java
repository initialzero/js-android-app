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

package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.model.ReportModel;
import com.jaspersoft.android.jaspermobile.presentation.presenter.ReportViewPresenter;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportView;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.jaspermobile.webview.JasperChromeClientListenerImpl;
import com.jaspersoft.android.jaspermobile.webview.SystemChromeClient;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.RestClient;
import com.jaspersoft.android.sdk.service.Session;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import roboguice.fragment.RoboFragment;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@EFragment(R.layout.report_html_viewer)
@OptionsMenu({R.menu.report_filter_manager_menu, R.menu.webview_menu})
public class ReportViewFragment extends RoboFragment implements ReportView {

    public static final String TAG = "report-view";
    private static final String MIME = "text/html";
    private static final String UTF_8 = "utf-8";

    @FragmentArg
    protected ResourceLookup resource;

    @ViewById
    protected JSWebView webView;
    @ViewById(android.R.id.empty)
    protected TextView errorView;
    @ViewById
    protected ProgressBar progressBar;

    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;

    @Inject
    protected JsRestClient jsRestClient;
    @Inject
    protected RestClient restClient;
    @Inject
    protected Session session;
    @Inject
    protected ReportParamsStorage paramsStorage;

    private ReportViewPresenter mPresenter;
    private ReportActionListener mActionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectComponents();
        mPresenter.init();
    }

    private void injectComponents() {
        ReportModel reportModel = new ReportModel(jsRestClient, session, resource.getUri());
        RequestExceptionHandler exceptionHandler = new RequestExceptionHandler(getActivity());
        mPresenter = new ReportViewPresenter(paramsStorage, exceptionHandler, reportModel);
        mPresenter.setView(this);
        mActionListener = mPresenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.destroy();
    }

    @AfterViews
    final void init() {
        scrollableTitleHelper.injectTitle(resource.getLabel());
        progressBar.setVisibility(View.VISIBLE);

        SystemChromeClient systemChromeClient = SystemChromeClient.from(getActivity())
                .withDelegateListener(new JasperChromeClientListenerImpl(progressBar));
        WebViewEnvironment.configure(webView)
                .withDefaultSettings()
                .withChromeClient(systemChromeClient);
    }

    @Override
    public void showLoading() {
        ProgressDialogFragment.builder(getFragmentManager()).show();
    }

    @Override
    public void hideLoading() {
        ProgressDialogFragment.dismiss(getFragmentManager());
    }

    @Override
    public void showError(String message) {
        errorView.setVisibility(View.VISIBLE);
        errorView.setText(message);
    }

    @Override
    public void hideError() {
        errorView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setFilterActionVisible(boolean showFilterActionVisible) {

    }

    @Override
    public void showFiltersPage() {

    }

    @Override
    public void showPage(String page) {
        webView.loadDataWithBaseURL(restClient.getServerUrl(), page, MIME, UTF_8, null);
    }
}
