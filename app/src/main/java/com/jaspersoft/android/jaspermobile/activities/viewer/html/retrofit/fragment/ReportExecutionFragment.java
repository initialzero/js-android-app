package com.jaspersoft.android.jaspermobile.activities.viewer.html.retrofit.fragment;

import android.app.Activity;
import android.content.DialogInterface;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.CommonRequestListener;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.ArrayList;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class ReportExecutionFragment extends RoboSpiceFragment {
    public static final String TAG = ReportExecutionFragment.class.getSimpleName();

    @FragmentArg
    ResourceLookup resource;
    @FragmentArg
    double versionCode;

    @Inject
    JsRestClient jsRestClient;

    // Stub
    public boolean isResourceLoaded() {
        return true;
    }

    public void executeReport(ArrayList<ReportParameter> reportParameters) {
        ReportExecutionRequest executionData = prepareExecutionData(reportParameters);
        final RunReportExecutionRequest request = new RunReportExecutionRequest(jsRestClient, executionData);
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
                getSpiceManager().execute(request, new RunReportExecutionListener());
            }
        };

        if (ProgressDialogFragment.isVisible(getFragmentManager())) {
            ProgressDialogFragment.getInstance(getFragmentManager())
                    .setOnCancelListener(cancelListener);
            // Send request
            showListener.onShow(null);
        } else {
            ProgressDialogFragment.show(getFragmentManager(), cancelListener, showListener);
        }
    }

    public void executeReport() {
        executeReport(new ArrayList<ReportParameter>());
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private ReportExecutionRequest prepareExecutionData(ArrayList<ReportParameter> reportParameters) {
        ReportExecutionRequest executionData = new ReportExecutionRequest();
        executionData.configureExecutionForProfile(jsRestClient);
        executionData.setReportUnitUri(resource.getUri());
        executionData.setMarkupType(ReportExecutionRequest.MARKUP_TYPE_EMBEDDABLE);
        executionData.setOutputFormat("html");
        executionData.setPages("1");
        executionData.setAsync(true);
        executionData.setInteractive(true);
        executionData.setFreshData(false);
        executionData.setSaveDataSnapshot(false);
        executionData.setAllowInlineScripts(false);
        executionData.setParameters(reportParameters);
        return executionData;
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class RunReportExecutionListener extends CommonRequestListener<ReportExecutionResponse> {
        @Override
        public void onSemanticFailure(SpiceException spiceException) {
            ProgressDialogFragment.dismiss(getFragmentManager());
        }

        public void onSemanticSuccess(ReportExecutionResponse data) {
            ProgressDialogFragment.dismiss(getFragmentManager());
        }

        @Override
        public Activity getCurrentActivity() {
            return getActivity();
        }
    }
}
