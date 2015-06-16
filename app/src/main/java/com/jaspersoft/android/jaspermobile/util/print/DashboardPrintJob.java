/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.print;

import android.annotation.TargetApi;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.TextUtils;
import android.webkit.WebView;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class DashboardPrintJob implements ResourcePrintJob {
    private final String printName;
    private final WebView webView;

    private DashboardPrintJob(Builder builder) {
        this.printName = builder.printName;
        this.webView = builder.webView;
    }

    public static Builder builder() {
        return new Builder();
    }

    @TargetApi(19)
    @Override
    public void printResource() {
        PrintManager printManager = (PrintManager) webView.getContext().getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(printName);
        printManager.print(printName, printAdapter, new PrintAttributes.Builder().build());
    }

    public static class Builder {
        private String printName;
        private WebView webView;

        public Builder setPrintName(String printName) {
            this.printName = printName;
            return this;
        }

        public Builder setWebView(WebView webView) {
            this.webView = webView;
            return this;
        }

        public ResourcePrintJob build() {
            validateDependencies();
            return new DashboardPrintJob(this);
        }

        private void validateDependencies() {
            if (webView == null) {
                throw new IllegalStateException("WebView should not be null");
            }
            if (TextUtils.isEmpty(printName)) {
                throw new IllegalStateException("Print name should not be null");
            }
        }
    }
}
