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

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.PrintRequest;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetPrintReportPageCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportTotalPagesPropertyCase;
import com.jaspersoft.android.jaspermobile.internal.di.ActivityContext;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@PerActivity
public final class ReportPrintJob implements ResourcePrintJob {

    private final Context mContext;
    private final GetReportTotalPagesPropertyCase mGetReportTotalPagesPropertyCase;
    private final GetPrintReportPageCase mGetPrintReportPageCase;

    @Inject
    public ReportPrintJob(
            @ActivityContext Context context,
            GetReportTotalPagesPropertyCase getReportTotalPagesPropertyCase,
            GetPrintReportPageCase getPrintReportPageCase
    ) {
        mContext = context;
        mGetReportTotalPagesPropertyCase = getReportTotalPagesPropertyCase;
        mGetPrintReportPageCase = getPrintReportPageCase;
    }

    @NonNull
    @Override
    public ResourcePrintJob printResource(@NonNull String resourceUri, @NonNull String printName) {
        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);

        PrintAttributes printAttributes = new PrintAttributes.Builder().build();
        PrintDocumentAdapter printAdapter = new Adapter(printName, resourceUri);

        printManager.print(printName, printAdapter, printAttributes);
        return this;
    }

    private void cancelTasks() {
        mGetReportTotalPagesPropertyCase.unsubscribe();
        mGetPrintReportPageCase.unsubscribe();
    }

    private class Adapter extends PrintDocumentAdapter {
        private final String mPrintName;
        private final String mResourceUri;
        public PageRangeFormat mPageRangeFormat;

        public Adapter(String printName, String resourceUri) {
            mPrintName = printName;
            mResourceUri = resourceUri;
        }

        @Override
        public void onLayout(
                PrintAttributes oldAttributes,
                PrintAttributes newAttributes,
                CancellationSignal cancellationSignal,
                final LayoutResultCallback callback,
                Bundle extras
        ) {
            if (cancellationSignal.isCanceled()) {
                cancelTasks();
                callback.onLayoutCancelled();
                return;
            }
            mGetReportTotalPagesPropertyCase.execute(mResourceUri, new SimpleSubscriber<Integer>() {
                @Override
                public void onError(Throwable throwable) {
                    callback.onLayoutFailed(throwable.getMessage());
                }

                @Override
                public void onNext(Integer pageCount) {
                    mPageRangeFormat = new PageRangeFormat(pageCount);
                    PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(mPrintName)
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(pageCount)
                            .build();
                    callback.onLayoutFinished(pdi, true);
                }
            });
        }


        @Override
        public void onWrite(
                final PageRange[] pages,
                final ParcelFileDescriptor destination,
                CancellationSignal cancellationSignal,
                final WriteResultCallback callback
        ) {
            if (cancellationSignal.isCanceled()) {
                cancelTasks();
                callback.onWriteCancelled();
                return;
            }

            String range = mPageRangeFormat.format(pages[0]);
            PageRequest page = new PageRequest.Builder()
                    .setRange(range)
                    .setUri(mResourceUri)
                    .asPdf()
                    .build();
            PrintRequest request = new PrintRequest(page, destination);
            mGetPrintReportPageCase.execute(request, new SimpleSubscriber<ParcelFileDescriptor>() {
                @Override
                public void onError(Throwable e) {
                    callback.onWriteFailed(e.getMessage());
                }

                @Override
                public void onNext(ParcelFileDescriptor page) {
                    callback.onWriteFinished(pages);
                }
            });
        }
    }
}
