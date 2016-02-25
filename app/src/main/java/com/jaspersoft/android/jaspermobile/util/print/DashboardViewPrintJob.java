package com.jaspersoft.android.jaspermobile.util.print;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.annotation.NonNull;
import android.webkit.WebView;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class DashboardViewPrintJob implements ResourcePrintJob {

    private final WebView webView;

    public DashboardViewPrintJob(WebView webView) {
        this.webView = webView;
    }

    @NonNull
    @TargetApi(19)
    @Override
    public ResourcePrintJob printResource(@NonNull Bundle args) {
        String printName = args.getString(ResourcePrintJob.PRINT_NAME_KEY);

        PrintManager printManager = (PrintManager) webView.getContext().getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            printAdapter = webView.createPrintDocumentAdapter(printName);
        } else {
            printAdapter = webView.createPrintDocumentAdapter();
        }
        printManager.print(printName, printAdapter, new PrintAttributes.Builder().build());
        return this;
    }
}
