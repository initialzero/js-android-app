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
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class ReportPrintJob implements ResourcePrintJob {
    private final Context mContext;
    private final PrintUnit printUnit;
    private final String printName;

    private ReportPrintJob(@NonNull Builder builder) {
        mContext = builder.context;
        printUnit = builder.printUnit;
        printName = builder.printName;
    }

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    @TargetApi(19)
    @Override
    public void printResource() {
        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);
        String jobName = printName;

        PrintAttributes printAttributes = new PrintAttributes.Builder().build();
        PrintDocumentAdapter printAdapter = new PrintReportAdapter();

        printManager.print(jobName, printAdapter, printAttributes);
    }

    @TargetApi(19)
    private class PrintReportAdapter extends PrintDocumentAdapter {
        private Subscription getPageCountTask;
        private Subscription writeContentTask;

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal, final LayoutResultCallback callback, Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                if (getPageCountTask != null) {
                    getPageCountTask.unsubscribe();
                }
                callback.onLayoutCancelled();
                return;
            }

            getPageCountTask = printUnit.getPageCount()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<Integer>() {
                                @Override
                                public void call(Integer number) {
                                    PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(printName)
                                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                            .setPageCount(number)
                                            .build();
                                    callback.onLayoutFinished(pdi, true);
                                }
                            },
                            new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    callback.onLayoutFailed(throwable.getMessage());
                                }
                            });
        }

        @Override
        public void onWrite(PageRange[] pages, final ParcelFileDescriptor destination,
                            CancellationSignal cancellationSignal, final WriteResultCallback callback) {
            if (cancellationSignal.isCanceled()) {
                if (writeContentTask != null) {
                    writeContentTask.unsubscribe();
                }
                callback.onWriteCancelled();
                return;
            }

            writeContentTask = printUnit.writeContent(destination)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<Object>() {
                                @Override
                                public void call(Object o) {
                                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                                }
                            },
                            new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    callback.onWriteFailed(throwable.getMessage());
                                }
                            });
        }
    }

    public static class Builder {
        private final Context context;
        private String printName;
        private PrintUnit printUnit;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setPrintUnit(@Nullable PrintUnit printUnit) {
            this.printUnit = printUnit;
            return this;
        }

        public Builder setPrintName(@Nullable String printName) {
            this.printName = printName;
            return this;
        }

        public ResourcePrintJob build() {
            validateDependencies();
            return new ReportPrintJob(this);
        }

        private void validateDependencies() {
            if (printUnit == null) {
                throw new IllegalStateException("Print unit should not be null");
            }
            if (TextUtils.isEmpty(printName)) {
                throw new IllegalStateException("Job print name should not be null. Current value: " + printName);
            }
        }
    }
}
