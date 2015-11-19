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

package com.jaspersoft.android.jaspermobile.util.print;

import android.annotation.TargetApi;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class ReportPrintJob implements ResourcePrintJob {

    private final Context mContext;
    private final PrintUnit printUnit;
    private final String printName;

    ReportPrintJob(Context mContext, PrintUnit printUnit, String printName) {
        if (mContext == null) {
            throw new IllegalArgumentException("Context should not be null");
        }
        if (printUnit == null) {
            throw new IllegalArgumentException("Print unit should not be null");
        }
        if (TextUtils.isEmpty(printName)) {
            throw new IllegalArgumentException("Print name should not be null");
        }

        this.mContext = mContext;
        this.printUnit = printUnit;
        this.printName = printName;
    }

    @NonNull
    @TargetApi(19)
    @Override
    public ResourcePrintJob printResource() {
        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);

        PrintAttributes printAttributes = new PrintAttributes.Builder().build();
        PrintDocumentAdapter printAdapter = new PrintReportAdapter(printUnit, printName);

        printManager.print(printName, printAdapter, printAttributes);
        return this;
    }

}
