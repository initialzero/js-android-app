package com.jaspersoft.android.jaspermobile.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.util.print.ResourcePrintJob;
import com.jaspersoft.android.jaspermobile.util.print.ResourceProvider;
import com.jaspersoft.android.jaspermobile.util.print.ReportPrintJob;
import com.jaspersoft.android.jaspermobile.util.print.StreamReportResourceProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.BaseRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.springframework.http.client.ClientHttpResponse;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */

public class PrintReportHelper {

    @TargetApi(19)
    public static void printDashboard(WebView webViewToPrint, String dashboardName){
        PrintManager printManager = (PrintManager) webViewToPrint.getContext().getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webViewToPrint.createPrintDocumentAdapter(dashboardName);
        printManager.print(dashboardName, printAdapter,
                new PrintAttributes.Builder().build());
    }

    @TargetApi(19)
    public static void printReport(JsRestClient jsRestClient, final Context context, ResourceLookup resource, ArrayList<ReportParameter> reportParameters){
        ResourceProvider<ClientHttpResponse> fileResourceProvider = StreamReportResourceProvider.builder()
                .setResource(resource)
                .setJsRestClient(jsRestClient)
                .addReportParameters(reportParameters)
                .build();

        ResourcePrintJob printJob = ReportPrintJob.builder(context)
                .setResourceProvider(fileResourceProvider)
                .setPrintName(String.valueOf(new Random().nextInt(1000)))
                .build();

        printJob.printResource();
    }

}
