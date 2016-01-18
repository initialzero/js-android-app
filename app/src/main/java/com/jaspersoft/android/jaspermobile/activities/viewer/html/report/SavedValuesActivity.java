package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetReportOptionResourceRequest;
import com.jaspersoft.android.sdk.client.async.request.GetReportResourceRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ReportOptionLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */

@EActivity
public class SavedValuesActivity extends RoboSpiceActivity {

    @Extra
    protected ResourceLookup resourceLookup;

    @Inject
    protected JsRestClient jsRestClient;

    @Inject
    protected ReportParamsStorage paramsStorage;

    @Bean
    protected ResourceOpener resourceOpener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showSavedValuesTitle();

        getSavedValuesInfo();
        showProgressDialog();
    }

    private void showProgressDialog() {
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        }
                )
                .show();
    }

    private void showSavedValuesTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(resourceLookup.getLabel());
        }
    }

    private void getSavedValuesInfo() {
        GetReportOptionResourceRequest reportOptionResourceRequest = new GetReportOptionResourceRequest(jsRestClient, resourceLookup.getUri());
        getSpiceManager().execute(reportOptionResourceRequest, new SavedValuesRequestListener());
    }

    private void getReportLookup(String reportUri) {
        GetReportResourceRequest reportResourceRequest = new GetReportResourceRequest(jsRestClient, reportUri);
        getSpiceManager().execute(reportResourceRequest, new ReportLookupListener());
    }

    private class SavedValuesRequestListener extends SimpleRequestListener<ReportOptionLookup> {
        @Override
        protected Context getContext() {
            return SavedValuesActivity.this;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            super.onRequestFailure(spiceException);
            finish();
        }

        @Override
        public void onRequestSuccess(ReportOptionLookup reportOptionLookup) {
            String reportUri = reportOptionLookup.getReportUri();
            paramsStorage.getInputControlHolder(reportUri).setReportParams(reportOptionLookup.getReportParameters());
            getReportLookup(reportUri);
        }
    }

    private class ReportLookupListener extends SimpleRequestListener<ResourceLookup> {
        @Override
        protected Context getContext() {
            return SavedValuesActivity.this;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            super.onRequestFailure(spiceException);
            finish();
        }

        @Override
        public void onRequestSuccess(ResourceLookup resourceLookup) {
            resourceOpener.openResource(null, resourceLookup);

            finish();
        }
    }
}
