/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

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
