package com.jaspersoft.android.jaspermobile.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.util.print.AppPrinter;
import com.jaspersoft.android.jaspermobile.util.print.CommonPrintJob;
import com.jaspersoft.android.jaspermobile.util.print.FileResourceProvider;
import com.jaspersoft.android.jaspermobile.util.print.ReportResourceProvider;
import com.jaspersoft.android.jaspermobile.util.print.ResourcePrinter;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.BaseRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.ArrayList;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */

public class PrintReportHelper {

    public static BaseRequest request;

    @TargetApi(19)
    public static void printDashboard(WebView webViewToPrint, String dashboardName){
        PrintManager printManager = (PrintManager) webViewToPrint.getContext().getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webViewToPrint.createPrintDocumentAdapter(dashboardName);
        printManager.print(dashboardName, printAdapter,
                new PrintAttributes.Builder().build());
    }

    @TargetApi(19)
    public static void printReport(JsRestClient jsRestClient, Context context, ResourceLookup resource, ArrayList<ReportParameter> reportParameters){
        FileResourceProvider fileResourceProvider = ReportResourceProvider.builder(context)
                .setResource(resource)
                .setJsRestClient(jsRestClient)
                .addReportParameters(reportParameters)
                .build();

        ResourcePrinter printer = AppPrinter.builder()
                .setResourceProvider(fileResourceProvider)
                .setResourcePrintJob(CommonPrintJob.newInstance(context))
                .build();

        printer.print();
    }

}
