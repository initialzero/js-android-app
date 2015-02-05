package com.jaspersoft.android.jaspermobile.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.BaseRequest;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveExportOutputRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */

public class PrintReportHelper {

    public static BaseRequest request;

    @TargetApi(19)
    public static void printDashboard(Context context, WebView webViewToPrint, String dashboardName){
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webViewToPrint.createPrintDocumentAdapter();
        printManager.print(dashboardName, printAdapter,
                new PrintAttributes.Builder().build());
    }

    @TargetApi(19)
    public static void printReport(final JsRestClient jsRestClient, final Context context, ResourceLookup resource, ArrayList<ReportParameter> reportParameters){
        final PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        final File testPdfFile = new File(context.getExternalFilesDir(null), resource.getLabel() + ".pdf");
        final ReportExecutionRequest executionRequest = new ReportExecutionRequest();
        executionRequest.setReportUnitUri(resource.getUri());
        executionRequest.setInteractive(false);
        executionRequest.setOutputFormat("PDF");
        executionRequest.setEscapedAttachmentsPrefix("./");

        if (reportParameters != null && !reportParameters.isEmpty()) {
            executionRequest.setParameters(reportParameters);
        }

        final RequestListener<File> saveFileListener = new RequestListener<File>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                ProgressDialogFragment.dismiss(((RoboSpiceFragmentActivity) context).getSupportFragmentManager());
            }

            @Override
            public void onRequestSuccess(File file) {
                ProgressDialogFragment.dismiss(((RoboSpiceFragmentActivity) context).getSupportFragmentManager());
                String jobName = file.getName();
                printManager.print(jobName, new PrintReportAdapter(file),
                        new PrintAttributes.Builder().build());
            }
        };

        final RequestListener<ReportExecutionResponse> runReportListener = new RequestListener<ReportExecutionResponse>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                ProgressDialogFragment.dismiss(((RoboSpiceFragmentActivity) context).getSupportFragmentManager());
            }

            @Override
            public void onRequestSuccess(ReportExecutionResponse response) {
                ExportExecution execution = response.getExports().get(0);
                String exportOutput = execution.getId();
                String executionId = response.getRequestId();

                // save report file
                request = new SaveExportOutputRequest(jsRestClient,
                        executionId, exportOutput, testPdfFile);
                ((RoboSpiceFragmentActivity) context).getSpiceManager().execute(request, saveFileListener);
            }
        };

        DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!request.isCancelled()) {
                    ((RoboSpiceFragmentActivity) context).getSpiceManager().cancel(request);
                }
            }
        };

        DialogInterface.OnShowListener showListener = new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                request = new RunReportExecutionRequest(jsRestClient, executionRequest);
                ((RoboSpiceFragmentActivity) context).getSpiceManager().execute(request, runReportListener);
            }
        };

        ProgressDialogFragment.builder(((RoboSpiceFragmentActivity) context).getSupportFragmentManager())
                .setOnCancelListener(cancelListener)
                .setOnShowListener(showListener)
                .setLoadingMessage(R.string.loading_msg)
                .show();

    }

    @TargetApi(19)
    private static class PrintReportAdapter extends PrintDocumentAdapter {

        private File fileToPrint;

        public PrintReportAdapter(File fileToPrint) {
            this.fileToPrint = fileToPrint;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(fileToPrint.getName()).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();
            callback.onLayoutFinished(pdi, true);
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
            InputStream input = null;
            OutputStream output = null;

            try {

                input = new FileInputStream(fileToPrint);
                output = new FileOutputStream(destination.getFileDescriptor());

                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }

                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

            } catch (Exception e) {
                //Catch exception
            } finally {
                try {
                    input.close();
                    output.close();
                } catch (Exception e) {
                    //Catch exception
                }
            }
        }
    }
}
