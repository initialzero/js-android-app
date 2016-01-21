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

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.ReportOptionHolder;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.option.ReportOption;
import com.jaspersoft.android.sdk.client.oxm.report.option.ReportOptionResponse;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment
public class ReportOptionsFragmentDialog extends BaseDialogFragment implements DialogInterface.OnShowListener, AdapterView.OnItemClickListener {

    private static final String REPORT_URI_ARG = "report_uri";

    @Inject
    protected ReportParamsStorage paramsStorage;

    @Inject
    protected JsRestClient jsRestClient;

    @Inject
    protected Analytics analytics;

    private String reportUri;
    private List<ReportOption> mReportOptions;

    private ProgressBar loadingBar;
    private ListView reportOptionsList;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View reportOptionLayout = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_report_options, null);

        loadingBar = (ProgressBar) reportOptionLayout.findViewById(R.id.reportOptionsProgress);
        reportOptionsList = (ListView) reportOptionLayout.findViewById(R.id.reportOptionsList);

        builder.setTitle(R.string.ro_show);
        builder.setView(reportOptionLayout);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.setOnShowListener(this);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        requestReportOptions();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mReportOptions != null && position < mReportOptions.size()) {
            List<ReportOptionHolder> reportOptionHolders = convertToHolder(position);
            paramsStorage.getInputControlHolder(reportUri).setReportOptions(reportOptionHolders);
            ((ReportOptionsDialogClickListener) mDialogListener).onOptionSelected(mReportOptions.get(position));
        }
        dismiss();
    }

    @Override
    protected Class<ReportOptionsDialogClickListener> getDialogCallbackClass() {
        return ReportOptionsDialogClickListener.class;
    }

    public static ReportOptionDialogFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new ReportOptionDialogFragmentBuilder(fragmentManager);
    }

    @Background
    protected void requestReportOptions(){
        mReportOptions = new ArrayList<>();
        mReportOptions.add(new ReportOption(reportUri, reportUri, getString(R.string.ro_default)));

        ReportOptionResponse reportOptionResponse = jsRestClient.getReportOptionsList(reportUri);
        mReportOptions.addAll(reportOptionResponse.getOptions());
        showReportOptions();
        analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.REPORT_OPTIONS_VIEWED.getValue(), null);
    }

    @UiThread
    protected void showReportOptions() {
        loadingBar.setVisibility(View.GONE);

        ArrayAdapter<String> reportOptionArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, convertToStringList());
        reportOptionsList.setAdapter(reportOptionArrayAdapter);
        reportOptionsList.setVisibility(View.VISIBLE);
        reportOptionsList.setOnItemClickListener(this);
    }

    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(REPORT_URI_ARG)) {
                reportUri = args.getString(REPORT_URI_ARG);
            }
        }
    }

    private List<String > convertToStringList(){
        if (mReportOptions == null) return new ArrayList<>();

        List<String> reportOptionsTitles = new ArrayList<>();
        for (ReportOption mReportOption : mReportOptions) {
            reportOptionsTitles.add(mReportOption.getLabel());
        }
        return reportOptionsTitles;
    }

    private List<ReportOptionHolder> convertToHolder(int seelctedIndex){
        List<ReportOptionHolder> reportOptionHolders = new ArrayList<>();
        for (int i = 0; i < mReportOptions.size(); i++) {
            ReportOptionHolder reportOptionHolder = new ReportOptionHolder(mReportOptions.get(i), null);
            reportOptionHolder.setSelected(seelctedIndex == i);
            reportOptionHolders.add(reportOptionHolder);
        }
        return reportOptionHolders;
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class ReportOptionDialogFragmentBuilder extends BaseDialogFragmentBuilder<ReportOptionsFragmentDialog> {

        public ReportOptionDialogFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public ReportOptionDialogFragmentBuilder setReportUri(String reportLink) {
            args.putString(REPORT_URI_ARG, reportLink);
            return this;
        }

        @Override
        protected ReportOptionsFragmentDialog build() {
            return new ReportOptionsFragmentDialog_();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface ReportOptionsDialogClickListener extends DialogClickListener {
        void onOptionSelected(ReportOption reportOption);
    }
}
